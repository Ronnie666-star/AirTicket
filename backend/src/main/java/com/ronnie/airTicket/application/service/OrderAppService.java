package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.FlightNotFoundException;
import com.ronnie.airTicket.domain.exception.ForbiddenException;
import com.ronnie.airTicket.domain.exception.OrderNotFoundException;
import com.ronnie.airTicket.domain.model.flight.CabinClass;
import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.model.order.Order;
import com.ronnie.airTicket.domain.model.order.OrderStatus;
import com.ronnie.airTicket.domain.model.order.PayStatus;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import com.ronnie.airTicket.infrastructure.config.DefaultChannelSeeder;
import com.ronnie.airTicket.infrastructure.mapper.ChannelMapper;
import com.ronnie.airTicket.infrastructure.mapper.OrderMapper;
import com.ronnie.airTicket.infrastructure.persistence.po.ChannelPO;
import com.ronnie.airTicket.infrastructure.persistence.query.OrderSearchQO;
import com.ronnie.airTicket.interfaces.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单用例。
 * 读侧（search）：只读，直接注入 OrderMapper 查 QO（分页由 count + LIMIT 完成），不走 Repository；
 * 写侧（book / pay / confirm / cancel / verify / reschedule）：走 domain 的 OrderRepository 加载聚合、改聚合、再存回。
 * 写用例都标 @Transactional 且用 findByIdForUpdate / FlightRepository.findByIdForUpdate 加锁：
 *   下单减库存、退订还座位、改签换座位，都是典型的"读-改-写"，不锁会出并发问题。
 * 归属校验：支付 / 退订 / 核销 / 改签 / 确认支付 / 详情 都必须操作本人订单，否则 403（不泄露订单归属）。
 */
@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final FlightRepository flightRepository;
    private final ChannelMapper channelMapper;
    private final PaymentOrderStore paymentOrderStore;

    /** 下单默认挂"官方网站"渠道：启动自愈会确保它存在，这里实时查回其 id（不硬编码 1）。 */
    private Long defaultChannelId() {
        ChannelPO channel = channelMapper.findByName(DefaultChannelSeeder.DEFAULT_CHANNEL_NAME);
        return channel == null ? 1L : channel.getId();   // 极端兜底：渠道缺失仍能下单，正常由启动自愈保证
    }

    // ===== 读侧：查询不走 Repository =====

    public PageResult<OrderQueryResult> search(OrderQueryCommand cmd) {
        int page = PageResult.normalizePage(cmd.page());
        int size = PageResult.normalizeSize(cmd.size());
        long total = orderMapper.countSearch(
                cmd.userId(), cmd.code(), cmd.payStatus(), cmd.orderStatus(),
                cmd.createAtEarliest(), cmd.createAtLatest(),
                cmd.regionDep(), cmd.regionArr(), cmd.airlineName());
        List<OrderQueryResult> data = orderMapper.search(
                cmd.userId(), cmd.code(), cmd.payStatus(), cmd.orderStatus(),
                cmd.createAtEarliest(), cmd.createAtLatest(),
                cmd.regionDep(), cmd.regionArr(), cmd.airlineName(),
                (page - 1) * size, size
        ).stream().map(this::toResult).toList();
        return PageResult.of(total, page, size, data);
    }

    private OrderQueryResult toResult(OrderSearchQO qo) {
        return new OrderQueryResult(
                qo.getId(), qo.getIdFlight(), qo.getIdUser(), qo.getIdChannel(), qo.getCode(),
                qo.getCabinClass(),
                qo.getTotalPrice(), qo.getTotalTax(),
                qo.getPayStatus(), qo.getOrderStatus(),
                qo.getPayTime(), qo.getIssueTime(), qo.getCancelTime(),
                qo.getRemark(), qo.getCreateAt(),
                qo.getFlightCode(), qo.getRegionDep(), qo.getRegionArr(), qo.getAirlineName()
        );
    }

    /** 订单详情：归属不符 / 不存在统一 404（不泄露订单归属）。 */
    public OrderDetailResult detail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        return OrderDetailResult.from(order);
    }

    // ===== 写侧：走 Repository（全部加锁读 + 事务） =====

    /**
     * 下单订票：锁航班减所选舱级一张余票（防超卖），再建待出票/未支付订单，总价 = 所选舱级票价。
     * 订单号唯一索引碰撞自动重试。
     */
    @Transactional
    public OrderDetailResult book(Long userId, OrderBookCommand cmd) {
        CabinClass cabin = cmd.cabinClass() == null ? CabinClass.ECONOMY_CLASS : cmd.cabinClass();
        Flight flight = flightRepository.findByIdForUpdate(cmd.flightId())
                .orElseThrow(() -> new FlightNotFoundException(cmd.flightId()));
        if (flight.isCancelled()) {
            throw new DomainException("航班已取消，无法下单");
        }
        flight.decrementSeat(cabin);
        flightRepository.save(flight);

        // 订单号唯一索引兜底：生成撞号（同毫秒 + 同随机段，概率极低）就换一个号重试，最多 3 次
        Order order = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                order = new Order(
                        null, flight.getId(), userId, defaultChannelId(), generateOrderCode(), cabin,
                        flight.priceOf(cabin), BigDecimal.ZERO, PayStatus.UNPAID, OrderStatus.PENDING_TICKET_ISSUANCE,
                        null, null, null, cmd.remark(), null
                );
                orderRepository.save(order);
                break;
            } catch (DuplicateKeyException e) {
                if (attempt == 2) {
                    throw e;   // 连续 3 次撞号，极低概率，直接报"数据已存在"
                }
                // 否则换一个订单号重试；InnoDB 唯一键冲突是语句级错误，当前事务仍可用
            }
        }
        // 重新读一遍，把数据库 DEFAULT CURRENT_TIMESTAMP 生成的 createAt 带进响应
        Long orderId = order.getId();
        Order saved = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderDetailResult.from(saved);
    }

    /**
     * 发起支付（两段式第一步）：本人订单 + UNPAID -> PROCESSING（不改订单状态、不填支付/出票时间），
     * 生成模拟渠道支付单（内存 PaymentOrderStore），返回支付单号与待付金额。
     * 座位账：下单已扣座；若上次支付失败已回补余票（FAILED 支付单在），重新发起要再扣回这张座，
     * 否则"失败->重付->成功"的订单会变成不占座的已支付单（可超售）。
     */
    @Transactional
    public PayResult pay(Long userId, Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwner(order, userId);
        boolean seatReturnedOnLastFailure = isLastPaymentFailed(order.getId());
        order.pay();   // UNPAID -> PROCESSING；PROCESSING 重复支付 / 其他状态都会抛 400
        if (seatReturnedOnLastFailure) {
            Flight flight = flightRepository.findByIdForUpdate(order.getFlightId())
                    .orElseThrow(() -> new FlightNotFoundException(order.getFlightId()));
            flight.decrementSeat(order.getCabinClass());
            flightRepository.save(flight);
        }
        orderRepository.save(order);

        paymentOrderStore.removeByOrderId(order.getId());
        PaymentOrder paymentOrder = new PaymentOrder(
                generatePaymentNo(), order.getId(), userId, order.getChannelId(),
                order.getTotalPrice(), PaymentOrder.PaymentStatus.PENDING);
        paymentOrderStore.put(paymentOrder);
        return new PayResult(paymentOrder.paymentNo(), paymentOrder.amount(), OrderDetailResult.from(order));
    }

    /**
     * 用户面确认支付（两段式第二步）：本人订单 + PROCESSING 才能确认。
     * 成功 -> PAID + ISSUED_TICKET + 记 payTime/issueTime；失败 -> 回 UNPAID + PENDING_TICKET_ISSUANCE + 回补余票。
     */
    @Transactional
    public OrderDetailResult confirm(Long userId, Long orderId, Boolean success) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwner(order, userId);
        return confirmOrder(order, Boolean.TRUE.equals(success));
    }

    /**
     * 模拟渠道回调确认支付：凭支付单号确认（不校验登录，模拟第三方回告）。
     * 与用户面确认复用同一 {@link #confirmOrder} 逻辑，状态机一致。
     */
    @Transactional
    public OrderDetailResult channelCallback(String paymentNo, Boolean success) {
        PaymentOrder paymentOrder = paymentOrderStore.get(paymentNo)
                .orElseThrow(() -> new DomainException("支付单不存在"));
        Order order = orderRepository.findByIdForUpdate(paymentOrder.orderId())
                .orElseThrow(() -> new OrderNotFoundException(paymentOrder.orderId()));
        return confirmOrder(order, Boolean.TRUE.equals(success));
    }

    /** 支付单状态查询：归属校验（只能查自己的支付单）。 */
    public PaymentStatusResult payStatus(Long userId, String paymentNo) {
        PaymentOrder paymentOrder = paymentOrderStore.get(paymentNo)
                .orElseThrow(() -> new DomainException("支付单不存在"));
        if (!paymentOrder.userId().equals(userId)) {
            throw new ForbiddenException("无权限查看该支付单");
        }
        return new PaymentStatusResult(
                paymentOrder.paymentNo(), paymentOrder.orderId(), paymentOrder.channelId(),
                paymentOrder.amount(), paymentOrder.status().name());
    }

    /**
     * 启动自愈：把一张遗留 PROCESSING 订单回退 UNPAID + PENDING_TICKET_ISSUANCE 并回补余票。
     * 由 ApplicationReadyEvent 监听器逐单调用；单张失败不阻塞其余订单。
     */
    @Transactional
    public void healOneProcessingOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getPayStatus() != PayStatus.PROCESSING) {
            return;   // 已被并发确认/处理过，跳过
        }
        order.confirmFailed();   // 回退 UNPAID + PENDING_TICKET_ISSUANCE、清空支付/出票时间
        releaseSeat(order.getFlightId(), order.getCabinClass());
        orderRepository.save(order);
    }

    /** 启动自愈收尾：清空内存支付单（残留支付单号失效）。 */
    public void clearPaymentStore() {
        paymentOrderStore.clear();
    }

    /** 退订/取消：本人订单。未支付直接取消（refundAmount=0）；已支付走退订退款（refund = max(0, 总价-退票费)）。都释放该舱余票。 */
    @Transactional
    public OrderDetailResult cancel(Long userId, Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwner(order, userId);
        BigDecimal refundAmount = BigDecimal.ZERO;
        if (order.getPayStatus() == PayStatus.UNPAID) {
            order.cancelUnpaid();
            // 座位账：上次支付失败已回补余票的未支付单，取消时不再重复回补
            if (!isLastPaymentFailed(order.getId())) {
                releaseSeat(order.getFlightId(), order.getCabinClass());
            }
        } else {
            order.cancelRefund();
            Flight flight = flightRepository.findByIdForUpdate(order.getFlightId())
                    .orElseThrow(() -> new FlightNotFoundException(order.getFlightId()));
            refundAmount = order.getTotalPrice().subtract(flight.getCancellationFee());
            if (refundAmount.signum() < 0) {
                refundAmount = BigDecimal.ZERO;   // 退票费 >= 票价时按 0 退，不为负
            }
            releaseSeat(order.getFlightId(), order.getCabinClass());
        }
        orderRepository.save(order);
        return OrderDetailResult.from(order, refundAmount, null);
    }

    /** 核销：本人订单，已出票 -> 已核销。 */
    @Transactional
    public OrderDetailResult verify(Long userId, Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwner(order, userId);
        order.verify();
        orderRepository.save(order);
        return OrderDetailResult.from(order);
    }

    /**
     * 改签：本人订单，换同一航线、未起飞且未取消的航班，同舱互换余票 + 多退少补。
     * 可改签的订单：已出票（普通改签）；或航班已取消且已退款的订单（取消时已回补座位，改签只换航班不退旧座）。
     * priceDiff = 新航班该舱票价 - 旧航班该舱票价（正=补差、负=应退），放入 adjustAmount。
     */
    @Transactional
    public OrderDetailResult reschedule(Long userId, Long orderId, OrderRescheduleCommand cmd) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        assertOwner(order, userId);
        if (cmd.newFlightId().equals(order.getFlightId())) {
            throw new DomainException("改签目标航班与当前航班相同");
        }
        Flight oldFlight = flightRepository.findByIdForUpdate(order.getFlightId())
                .orElseThrow(() -> new FlightNotFoundException(order.getFlightId()));
        Flight newFlight = flightRepository.findByIdForUpdate(cmd.newFlightId())
                .orElseThrow(() -> new FlightNotFoundException(cmd.newFlightId()));

        boolean normalReschedule = order.getOrderStatus() == OrderStatus.ISSUED_TICKET;
        boolean flightCancelledReschedule = order.getOrderStatus() == OrderStatus.CANCELLED
                && order.getPayStatus() == PayStatus.REFUNDED && oldFlight.isCancelled();
        if (!normalReschedule && !flightCancelledReschedule) {
            throw new DomainException("只有已出票或航班已取消且已退款的订单才能改签");
        }
        // 限制改签时间：普通改签要求旧航班未起飞；航班取消的改签不受此限（取消航班出发时间已在过去）
        if (normalReschedule && !oldFlight.getDatetimeDep().isAfter(LocalDateTime.now())) {
            throw new DomainException("航班已起飞，无法改签");
        }
        // 限制改签航线：新航班必须与旧航班同一起降地区（"同航线"）
        if (!oldFlight.getRegionDep().equals(newFlight.getRegionDep())
                || !oldFlight.getRegionArr().equals(newFlight.getRegionArr())) {
            throw new DomainException("只能改签到相同航线的航班");
        }
        // 限制目标航班：未起飞、未取消
        if (!newFlight.getDatetimeDep().isAfter(LocalDateTime.now())) {
            throw new DomainException("目标航班已起飞，无法改签");
        }
        if (newFlight.isCancelled()) {
            throw new DomainException("目标航班已取消，无法改签");
        }
        // 同舱多退少补 + 同舱互换余票（舱级从订单取，改签不变）
        CabinClass cabin = order.getCabinClass();
        BigDecimal priceDiff = newFlight.priceOf(cabin).subtract(oldFlight.priceOf(cabin));
        if (normalReschedule) {
            oldFlight.incrementSeat(cabin);   // 普通改签才还旧座；航班取消的单取消时座位已回补，不再重复 +1
        }
        newFlight.decrementSeat(cabin);
        flightRepository.save(oldFlight);
        flightRepository.save(newFlight);

        order.reschedule(cmd.newFlightId(), priceDiff);
        orderRepository.save(order);
        return OrderDetailResult.from(order, null, priceDiff);
    }

    // ===== 私有方法 =====

    /** 归属校验：他人订单 -> 403（统一文案，不泄露归属）。 */
    private void assertOwner(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("无权限操作该订单");
        }
    }

    /** 支付确认的核心逻辑（用户面确认 / 渠道回调共用）：成功推进，失败回退并回补余票。 */
    private OrderDetailResult confirmOrder(Order order, boolean success) {
        if (success) {
            order.confirmPaid();
            markPayment(order.getId(), PaymentOrder.PaymentStatus.PAID);
        } else {
            order.confirmFailed();
            releaseSeat(order.getFlightId(), order.getCabinClass());
            markPayment(order.getId(), PaymentOrder.PaymentStatus.FAILED);
        }
        orderRepository.save(order);
        return OrderDetailResult.from(order);
    }

    private void markPayment(Long orderId, PaymentOrder.PaymentStatus status) {
        paymentOrderStore.findByOrderId(orderId).ifPresent(po ->
                paymentOrderStore.put(po.withStatus(status)));
    }

    /**
     * 该订单上一次支付是否以失败收场（内存支付单置 FAILED = 确认失败时已回补余票）。
     * 用于修正"失败->重付" / "失败->取消"的座位账，避免重复扣/回补。
     * 判定逻辑在 PaymentOrderStore（FlightAppService 取消航班时也会复用）。
     */
    private boolean isLastPaymentFailed(Long orderId) {
        return paymentOrderStore.isLastPaymentFailed(orderId);
    }

    /** 退订/取消/支付失败时把订单指向航班的对应舱余票还回去（也要锁，防并发）。 */
    private void releaseSeat(Long flightId, CabinClass cabin) {
        Flight flight = flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));
        flight.incrementSeat(cabin);
        flightRepository.save(flight);
    }

    /** 生成订单号：ORD + 毫秒时间戳 + 3 位随机数。唯一索引兜底 + book() 内重试。 */
    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    /** 生成支付单号：PAY + 毫秒时间戳 + 3 位随机数。 */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}

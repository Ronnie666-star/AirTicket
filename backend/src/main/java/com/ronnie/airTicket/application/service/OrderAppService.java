package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.FlightNotFoundException;
import com.ronnie.airTicket.domain.exception.OrderNotFoundException;
import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.model.order.Order;
import com.ronnie.airTicket.domain.model.order.OrderStatus;
import com.ronnie.airTicket.domain.model.order.PayStatus;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import com.ronnie.airTicket.infrastructure.mapper.FlightMapper;
import com.ronnie.airTicket.infrastructure.mapper.OrderMapper;
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
 * 写侧（book / pay / cancel / verify / reschedule）：走 domain 的 OrderRepository 加载聚合、改聚合、再存回。
 * 写用例都标 @Transactional 且用 findByIdForUpdate / FlightRepository.findByIdForUpdate 加锁：
 *   下单减库存、退订还座位、改签换座位，都是典型的"读-改-写"，不锁会出并发问题。
 */
@Service
@RequiredArgsConstructor
public class OrderAppService {

    /** 渠道表目前没有业务数据，下单统一挂到默认渠道 1（测试种子数据里建了 id=1 的"官方网站"）。 */
    private static final Long DEFAULT_CHANNEL_ID = 1L;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

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
                qo.getTotalPrice(), qo.getTotalTax(),
                qo.getPayStatus(), qo.getOrderStatus(),
                qo.getPayTime(), qo.getIssueTime(), qo.getCancelTime(),
                qo.getRemark(), qo.getCreateAt(),
                qo.getFlightCode(), qo.getRegionDep(), qo.getRegionArr(), qo.getAirlineName()
        );
    }

    // ===== 写侧：走 Repository（全部加锁读 + 事务） =====

    /** 下单订票：锁航班减一张经济舱余票（防超卖），再建待出票/未支付订单。订单号唯一索引碰撞自动重试。 */
    @Transactional
    public OrderDetailResult book(Long userId, OrderBookCommand cmd) {
        Flight flight = flightRepository.findByIdForUpdate(cmd.flightId())
                .orElseThrow(() -> new FlightNotFoundException(cmd.flightId()));
        flight.decrementEconomySeat();
        flightRepository.save(flight);

        // 订单号唯一索引兜底：生成撞号（同毫秒 + 同随机段，概率极低）就换一个号重试，最多 3 次
        Order order = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                order = new Order(
                        null, flight.getId(), userId, DEFAULT_CHANNEL_ID, generateOrderCode(),
                        flight.getPrice(), BigDecimal.ZERO, PayStatus.UNPAID, OrderStatus.PENDING_TICKET_ISSUANCE,
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

    /** 支付：未支付 -> 已支付 + 已出票。 */
    @Transactional
    public OrderDetailResult pay(Long id) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.pay();
        orderRepository.save(order);
        return OrderDetailResult.from(order);
    }

    /** 退订/取消：未支付直接取消，已支付走退订退款；都释放航班余票。 */
    @Transactional
    public OrderDetailResult cancel(Long id) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getPayStatus() == PayStatus.UNPAID) {
            order.cancelUnpaid();
        } else {
            order.cancelRefund();
        }
        releaseSeat(order.getFlightId());
        orderRepository.save(order);
        return OrderDetailResult.from(order);
    }

    /** 核销：已出票 -> 已核销。 */
    @Transactional
    public OrderDetailResult verify(Long id) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.verify();
        orderRepository.save(order);
        return OrderDetailResult.from(order);
    }

    /**
     * 改签：换订单指向的航班，多退少补。
     * 限制：只能改签到同一航司的航班；旧航班必须未起飞。
     * 座位：旧航班余票 +1，新航班余票 -1。
     */
    @Transactional
    public OrderDetailResult reschedule(Long id, OrderRescheduleCommand cmd) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (cmd.newFlightId().equals(order.getFlightId())) {
            throw new DomainException("改签目标航班与当前航班相同");
        }
        Flight oldFlight = flightRepository.findByIdForUpdate(order.getFlightId())
                .orElseThrow(() -> new FlightNotFoundException(order.getFlightId()));
        Flight newFlight = flightRepository.findByIdForUpdate(cmd.newFlightId())
                .orElseThrow(() -> new FlightNotFoundException(cmd.newFlightId()));

        // 限制改签时间：旧航班必须未起飞
        if (!oldFlight.getDatetimeDep().isAfter(LocalDateTime.now())) {
            throw new DomainException("航班已起飞，无法改签");
        }
        // 限制改签航司：新旧航班同一航司
        Long oldAirline = flightMapper.findAirlineIdByPlaneId(oldFlight.getIdPlane());
        Long newAirline = flightMapper.findAirlineIdByPlaneId(newFlight.getIdPlane());
        if (!oldAirline.equals(newAirline)) {
            throw new DomainException("只能改签到同一航司的航班");
        }
        // 多退少补：总价 += 新票价 - 旧票价
        BigDecimal priceDiff = newFlight.getPrice().subtract(oldFlight.getPrice());
        // 换座位：旧航班余票 +1，新航班余票 -1
        oldFlight.incrementEconomySeat();
        newFlight.decrementEconomySeat();
        flightRepository.save(oldFlight);
        flightRepository.save(newFlight);

        order.reschedule(cmd.newFlightId(), priceDiff);
        orderRepository.save(order);
        return OrderDetailResult.from(order);
    }

    /** 退订/取消时把订单指向航班的余票还回去（也要锁，防并发）。 */
    private void releaseSeat(Long flightId) {
        Flight flight = flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));
        flight.incrementEconomySeat();
        flightRepository.save(flight);
    }

    /** 生成订单号：ORD + 毫秒时间戳 + 3 位随机数。唯一索引兜底 + book() 内重试。 */
    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}

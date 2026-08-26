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
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSearchQO;
import com.ronnie.airTicket.interfaces.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 航班用例。
 * 读侧（search）：只读、不加载聚合根，直接注入基础设施层 FlightMapper 查 QO（分页由 count + LIMIT 完成）；
 * 写侧（insert / update / delete）：走 domain 的 FlightRepository 加载聚合、改聚合、再存回 —— 依赖倒置落地。
 * 写用例都标 @Transactional 且用 findByIdForUpdate 加锁：防并发删改。
 */
@Service
@RequiredArgsConstructor
public class FlightAppService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final OrderRepository orderRepository;
    private final FlightAccessGuard flightAccessGuard;
    private final PaymentOrderStore paymentOrderStore;

    // ===== 读侧：查询不走 Repository =====

    public PageResult<FlightQueryResult> search(FlightQueryCommand cmd) {
        int page = PageResult.normalizePage(cmd.page());
        int size = PageResult.normalizeSize(cmd.size());
        long total = flightMapper.countSearch(
                cmd.depCity(), cmd.arrCity(), cmd.depDate(),
                cmd.priceMin(), cmd.priceMax(), cmd.planeId(), cmd.airportName(), cmd.code(), cmd.now());
        List<FlightQueryResult> data = flightMapper.search(
                cmd.depCity(), cmd.arrCity(), cmd.depDate(),
                cmd.priceMin(), cmd.priceMax(), cmd.planeId(), cmd.airportName(), cmd.code(), cmd.now(),
                (page - 1) * size, size
        ).stream().map(this::toResult).toList();
        return PageResult.of(total, page, size, data);
    }

    private FlightQueryResult toResult(FlightSearchQO qo) {
        return new FlightQueryResult(
                qo.getId(), qo.getIdPlane(), qo.getIdAirportDep(), qo.getIdAirportArr(), qo.getCode(),
                qo.getDatetimeDep(), qo.getDatetimeArr(),
                qo.getRegionDep(), qo.getRegionArr(), qo.getGate(),
                qo.getDistance(), qo.getPrice(), qo.getPriceBusinessClass(), qo.getPriceFirstClass(),
                qo.getSeatFirstClass(), qo.getSeatBusinessClass(), qo.getSeatEconomyClass(),
                qo.getCancellationFee(), qo.getCreatedBy(),
                qo.getStatus()
        );
    }

    // ===== 详情（读侧） =====

    /** 航班详情：按 id 取完整航班信息，不存在 -> 404。 */
    public FlightDetailResult detail(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        return FlightDetailResult.from(flight);
    }

    // ===== 写侧：走 Repository =====

    /**
     * 创建航班（放票）：id 传 null（自增），create_at 交给数据库，
     * created_by 记为当前登录者 —— 谁放的票谁能编辑（管理员可管一切）。
     */
    @Transactional
    public FlightDetailResult insert(FlightInsertCommand cmd, CurrentUser user) {
        Flight flight = new Flight(
                null,
                cmd.idPlane(), cmd.idAirportDep(), cmd.idAirportArr(), cmd.code(),
                cmd.datetimeDep(), cmd.datetimeArr(),
                cmd.regionDep(), cmd.regionArr(), cmd.distance(),
                cmd.seatFirstClass(), cmd.seatBusinessClass(), cmd.seatEconomyClass(),
                cmd.price(), cmd.priceBusinessClass(), cmd.priceFirstClass(),
                cmd.cancellationFee(), cmd.gate(), cmd.status(),
                user.userId(), null
        );
        flightRepository.save(flight);   // 内部发现 id==null → INSERT，主键回填
        return FlightDetailResult.from(flight);
    }

    /**
     * 更新航班：先加锁读（FOR UPDATE）拿聚合，归属校验通过后改运行字段，最后存回。
     * 锁 + 行数校验两层防并发：锁保证"读-改-写"期间没有别的写/删交错；
     * save 返回 false（更新 0 行）说明这行在锁后已被并发删除，直接报"航班不存在"。
     */
    @Transactional
    public FlightDetailResult update(Long id, FlightUpdateCommand cmd, CurrentUser user) {
        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        flightAccessGuard.assertCanManage(user, flight);
        flight.update(
                cmd.datetimeDep(), cmd.datetimeArr(),
                cmd.seatFirstClass(), cmd.seatBusinessClass(), cmd.seatEconomyClass(),
                cmd.price(), cmd.priceBusinessClass(), cmd.priceFirstClass(),
                cmd.cancellationFee(), cmd.gate(), cmd.status()
        );
        if (!flightRepository.save(flight)) {   // 0 行 = 锁后已被并发删除
            throw new FlightNotFoundException(id);
        }
        return FlightDetailResult.from(flight);
    }

    /**
     * 删除航班（数据完整性保护）：先加锁读该行，确认归属且没有订单引用后才删。
     * 锁顺序固定为"先航班后订单"，避免与下单（先锁航班再插订单）形成死锁窗口；
     * 若删除时行已被并发删掉，delete 返回 0 行，报"航班不存在"。
     */
    @Transactional
    public void delete(Long id, CurrentUser user) {
        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        flightAccessGuard.assertCanManage(user, flight);
        if (orderRepository.countByFlightId(id) > 0) {
            throw new DomainException("该航班已有订单，无法删除");
        }
        if (!flightRepository.deleteById(id)) {
            throw new FlightNotFoundException(id);
        }
    }

    /**
     * 取消航班（模拟极端天气导致无法出行）：把航班置"已取消"，并对该航班下所有未结算终态的订单
     * 全额退款（免退票费）+ 置"已取消"，按是否占座回补余票，清掉残留支付单。
     * 锁顺序：先锁航班（FOR UPDATE），再逐单锁订单；与用户 cancel/pay 的 order->flight 顺序
     * 相反，极端并发下可能死锁（InnoDB 选一方回滚报 500）——课程项目接受的已知边界。
     * 已取消 / 已起飞航班不可取消；已结算终态订单（已取消 / 已退订）跳过，座位早已结算。
     */
    @Transactional
    public FlightCancelResult cancel(Long id, CurrentUser user) {
        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        flightAccessGuard.assertCanManage(user, flight);
        flight.cancel();   // 领域校验：未取消、未起飞

        int affected = 0, refunded = 0;
        BigDecimal refundTotal = BigDecimal.ZERO;
        for (Long orderId : orderRepository.findIdsByFlightId(id)) {
            Order order = orderRepository.findByIdForUpdate(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));
            OrderStatus os = order.getOrderStatus();
            if (os == OrderStatus.CANCELLED || os == OrderStatus.REFUNDED) {
                continue;   // 已结算终态：不改状态、不退款、不补座（取消/退订时座位已回补）
            }
            BigDecimal refund = order.getPayStatus() == PayStatus.PAID
                    ? order.getTotalPrice() : BigDecimal.ZERO;   // 全额退款，免退票费
            // 座位账：已支付/支付中的单占座；未支付但上次支付失败已回补的不占座
            boolean holdsSeat = order.getPayStatus() != PayStatus.UNPAID
                    || !paymentOrderStore.isLastPaymentFailed(order.getId());
            order.cancelByFlightCancellation();
            if (holdsSeat) {
                flight.incrementSeat(order.getCabinClass());   // 直接在已锁航班上回补，不二次锁
            }
            orderRepository.save(order);
            paymentOrderStore.removeByOrderId(order.getId());
            affected++;
            if (refund.signum() > 0) {
                refunded++;
                refundTotal = refundTotal.add(refund);
            }
        }
        flightRepository.save(flight);   // 状态 + 回补后的余票一起落库
        return FlightCancelResult.of(flight, affected, refunded, refundTotal);
    }
}

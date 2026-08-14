package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.FlightUnavailableException;
import com.ronnie.airTicket.domain.model.Flight;
import com.ronnie.airTicket.domain.model.Order;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用服务：只做"用例编排"，不写业务规则。
 * 它把几件已有的东西按顺序叫出来：加载聚合根 → 让领域层校验并扣减 → 落库兜底 → 建订单保存。
 * 事务边界就在这里：要么四步全成功，要么全部回滚。
 */
@Service
@RequiredArgsConstructor
public class BookTicketAppService {

    private final FlightRepository flightRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Order book(BookTicketCommand cmd) {
        // ① 加载聚合根
        Flight flight = flightRepository.findById(cmd.flightId());
        if (flight == null) {
            throw new FlightUnavailableException("航班不存在: " + cmd.flightId());
        }

        // ② 业务规则：flight.book() 内部校验状态、校验余票、扣减内存值
        flight.book(1);

        // ③ 并发兜底：条件 UPDATE，真正扣数据库里的余票；抢不到说明卖超了
        boolean booked = flightRepository.tryBook(flight.getId(), 1);
        if (!booked) {
            throw new FlightUnavailableException("余票不足或航班已不可售，请刷新重试");
        }

        // ④ 建订单 —— 价格从聚合根 Flight 取，不信任前端传价
        Order order = Order.create(flight.getId(), cmd.passengerName(), cmd.passengerPhone(), flight.getPrice());
        return orderRepository.save(order);
    }
}

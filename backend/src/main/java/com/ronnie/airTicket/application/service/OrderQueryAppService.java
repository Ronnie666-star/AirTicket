package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.Order;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 读侧用例：查我的订单列表。 */
@Service
@RequiredArgsConstructor
public class OrderQueryAppService {

    private final OrderRepository orderRepository;

    public List<Order> listAll() {
        return orderRepository.findAll();
    }
}

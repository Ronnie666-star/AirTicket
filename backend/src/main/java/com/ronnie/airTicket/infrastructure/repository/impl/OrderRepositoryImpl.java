package com.ronnie.airTicket.infrastructure.repository.impl;

import com.ronnie.airTicket.domain.model.Order;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import com.ronnie.airTicket.infrastructure.mapper.OrderMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.OrderAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.OrderPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderAssembler orderAssembler;

    @Override
    public Order save(Order order) {
        OrderPO po = orderAssembler.toPO(order);
        orderMapper.insert(po);
        order.assignId(po.getId());   // 自增主键回填到领域实体
        return order;
    }

    @Override
    public List<Order> findAll() {
        return orderMapper.findAll().stream()
                .map(orderAssembler::toDomain)
                .toList();
    }
}

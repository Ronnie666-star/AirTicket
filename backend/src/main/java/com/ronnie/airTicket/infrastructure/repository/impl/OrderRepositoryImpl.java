package com.ronnie.airTicket.infrastructure.repository.impl;

import com.ronnie.airTicket.domain.model.order.Order;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import com.ronnie.airTicket.infrastructure.mapper.OrderMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.OrderAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.OrderPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository 接口的 MyBatis 实现。
 * domain 层只认识 OrderRepository 接口，完全看不到这个类的存在 —— 依赖倒置落地。
 * save 是"聚合落库"的入口：id==null 走 INSERT（主键回填），否则走 UPDATE；
 * UPDATE 用返回的行数判断"这行是否真的还在"——0 行 = 已被并发删除，返回 false。
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderAssembler orderAssembler;

    @Override
    public Optional<Order> findById(Long id) {
        OrderPO po = orderMapper.findById(id);
        return Optional.ofNullable(orderAssembler.toDomain(po));
    }

    @Override
    public Optional<Order> findByIdForUpdate(Long id) {
        OrderPO po = orderMapper.findByIdForUpdate(id);
        return Optional.ofNullable(orderAssembler.toDomain(po));
    }

    @Override
    public boolean save(Order order) {
        OrderPO po = orderAssembler.toPO(order);
        if (order.getId() == null) {
            orderMapper.insert(po);           // INSERT，自增主键回填到 po.id
            order.assignId(po.getId());       // 再回填到领域聚合
            return true;                      // 新建不存在并发删除问题，恒为成功
        }
        return orderMapper.update(po) > 0;    // 0 行 = 这行在锁后已被并发删除
    }

    @Override
    public long countByFlightId(Long flightId) {
        return orderMapper.countByFlightId(flightId);
    }

    @Override
    public List<Long> findIdsByFlightId(Long flightId) {
        return orderMapper.findIdsByFlightId(flightId);
    }
}

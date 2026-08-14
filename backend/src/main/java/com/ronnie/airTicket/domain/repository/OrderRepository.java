package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.Order;

import java.util.List;

/** 订单仓库接口：领域层只声明"保存订单"，怎么落库是基础设施层的事。 */
public interface OrderRepository {

    Order save(Order order);

    /** 查询全部订单，按下单时间倒序。 */
    List<Order> findAll();
}

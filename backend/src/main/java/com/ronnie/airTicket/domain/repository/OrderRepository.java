package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.order.Order;

import java.util.Optional;

/**
 * 订单仓储端口（Port）。domain 层只声明"我要按 id 拿订单 / 存订单"，
 * MyBatis 怎么查怎么写是基础设施层的事 —— 依赖倒置让领域逻辑不依赖数据库。
 * 查询（search）不走这里，那是读侧，直接走 OrderMapper。
 */
public interface OrderRepository {

    /** 普通读：查询 / 展示路径用，不加锁。 */
    Optional<Order> findById(Long id);

    /** 加锁读：写路径"读-改-写"用（FOR UPDATE），防并发改单。调用方必须在 @Transactional 内。 */
    Optional<Order> findByIdForUpdate(Long id);

    /**
     * 保存聚合：内部判断 id==null 走 insert（主键回填），否则走 update。
     * @return 是否真正改了行。update 没碰到任何行（0 行，锁后已被并发删除）返回 false。
     */
    boolean save(Order order);

    /** 某航班下有多少订单（删除航班的保护性校验）。 */
    long countByFlightId(Long flightId);
}

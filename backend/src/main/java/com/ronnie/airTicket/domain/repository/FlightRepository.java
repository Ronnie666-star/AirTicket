package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.flight.Flight;

import java.util.Optional;

/**
 * 航班仓储端口（Port）。domain 层只声明"我要按 id 拿航班 / 存航班"，
 * MyBatis 怎么查怎么写是基础设施层的事 —— 依赖倒置让领域逻辑不依赖数据库。
 */
public interface FlightRepository {

    Optional<Flight> findById(Long id);

    /** 保存聚合：内部判断 id==null 走 insert（主键回填），否则走 update。 */
    void save(Flight flight);
}

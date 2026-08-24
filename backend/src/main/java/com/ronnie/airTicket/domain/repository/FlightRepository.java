package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.flight.Flight;

import java.util.Optional;

/**
 * 航班仓储端口（Port）。domain 层只声明"我要按 id 拿航班 / 存航班"，
 * MyBatis 怎么查怎么写是基础设施层的事 —— 依赖倒置让领域逻辑不依赖数据库。
 */
public interface FlightRepository {

    /** 普通读：查询 / 展示路径用，不加锁。 */
    Optional<Flight> findById(Long id);

    /** 加锁读：写路径"读-改-写"用（FOR UPDATE），防并发删改。调用方必须在 @Transactional 内。 */
    Optional<Flight> findByIdForUpdate(Long id);

    /**
     * 保存聚合：内部判断 id==null 走 insert（主键回填），否则走 update。
     * @return 是否真正改了行。update 没碰到任何行（0 行，锁后已被并发删除）返回 false。
     */
    boolean save(Flight flight);
}

package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.Flight;

import java.util.List;

/**
 * 仓库接口：领域层只声明"我需要什么数据操作"，不关心谁来干、怎么干。
 * 实现放在 infrastructure 层（MyBatis），这就是依赖倒置：领域不依赖基础设施。
 */
public interface FlightRepository {

    /** 按 id 加载聚合根（含余票、票价）；不存在返回 null。 */
    Flight findById(Long id);

    /** 查询所有可售航班（status=1），按起飞时间排序。 */
    List<Flight> findAvailable();

    /**
     * 并发安全扣减：只当"可售 且 余票足够"时才扣减成功。
     * 返回 false 说明并发下余票已被抢光 —— 数据库层面的超卖兜底。
     */
    boolean tryBook(Long flightId, int count);
}

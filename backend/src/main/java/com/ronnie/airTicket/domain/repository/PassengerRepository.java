package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.reference.Passenger;

import java.util.List;
import java.util.Optional;

/**
 * 常用乘机人仓储端口（Port）。
 * 乘客表是纯关系表，没有领域行为，不建聚合根 —— 领域侧用 Passenger 值对象表达。
 */
public interface PassengerRepository {

    /** 某用户添加的常用乘机人列表（JOIN sys_user 带姓名/用户名）。 */
    List<Passenger> listByUserId(Long userId);

    /** 按主键取一条（含 JOIN 信息），用于删除前归属校验。 */
    Optional<Passenger> findById(Long id);

    /** 是否已添加过同一乘机人（重复添加预判）。 */
    boolean existsByUserIdAndPassengerId(Long userId, Long passengerId);

    /**
     * 添加常用乘机人，主键回填到返回的 Passenger。
     * @return 新增记录；撞唯一约束（并发重复）返回 null。
     */
    Passenger add(Long userId, Long passengerId);

    /** 按主键删除。返回是否真正删了行（0 = 不存在 / 已被并发删除）。 */
    boolean deleteById(Long id);
}

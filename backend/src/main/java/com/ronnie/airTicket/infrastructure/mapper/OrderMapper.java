package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.OrderPO;
import com.ronnie.airTicket.infrastructure.persistence.query.OrderSearchQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 Mapper：只存在于基础设施层，是 SQL 唯一出现的地方。
 * search / countSearch 是"读侧查询"，动态 SQL 写在 OrderMapper.xml 里（<sql> 片段复用 WHERE）；
 * findById / findByIdForUpdate / insert / update 是"写侧"用的，返回/接收 OrderPO。
 */
@Mapper
public interface OrderMapper {

    // ===== 读侧：查询（返回查询形状 QO）=====

    /** 查自己的订单：userId 必传（README 约定 WHERE id_user = ?），其余筛选可空。 */
    List<OrderSearchQO> search(
            @Param("userId") Long userId,
            @Param("code") String code,
            @Param("payStatus") String payStatus,
            @Param("orderStatus") String orderStatus,
            @Param("createAtEarliest") LocalDateTime createAtEarliest,
            @Param("createAtLatest") LocalDateTime createAtLatest,
            @Param("regionDep") String regionDep,
            @Param("regionArr") String regionArr,
            @Param("airlineName") String airlineName,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /** 满足同一批筛选条件的总条数（分页 total）。 */
    long countSearch(
            @Param("userId") Long userId,
            @Param("code") String code,
            @Param("payStatus") String payStatus,
            @Param("orderStatus") String orderStatus,
            @Param("createAtEarliest") LocalDateTime createAtEarliest,
            @Param("createAtLatest") LocalDateTime createAtLatest,
            @Param("regionDep") String regionDep,
            @Param("regionArr") String regionArr,
            @Param("airlineName") String airlineName
    );

    /** 某航班下有多少订单：删除航班前做保护性校验。 */
    long countByFlightId(@Param("flightId") Long flightId);

    /** 按支付状态找订单 id（启动自愈回退遗留 PROCESSING 订单用）。 */
    List<Long> findIdsByPayStatus(@Param("payStatus") String payStatus);

    /** 某航班下的全部订单 id（航班取消时逐单退款 / 置取消用）。 */
    List<Long> findIdsByFlightId(@Param("flightId") Long flightId);

    // ===== 写侧：聚合读写（返回/接收 PO）=====

    OrderPO findById(@Param("id") Long id);

    /** 加锁读：写路径"读-改-写"用（FOR UPDATE），防并发改单。必须配合 @Transactional 使用。 */
    OrderPO findByIdForUpdate(@Param("id") Long id);

    /** 插入一行，自增主键回填到 po.getId()（useGeneratedKeys）。返回受影响行数。 */
    int insert(OrderPO po);

    /** 全字段更新。返回受影响行数（0 = 记录已被并发删除）。 */
    int update(OrderPO po);
}

package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.FlightPO;
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSearchQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 航班 Mapper：只存在于基础设施层，是 SQL 唯一出现的地方。
 * search / countSearch 是"读侧查询"，动态 SQL 写在 FlightMapper.xml 里（<sql> 片段复用 WHERE）；
 * findById / findByIdForUpdate / insert / update / delete 是"写侧"用的，返回/接收 FlightPO。
 */
@Mapper
public interface FlightMapper {

    // ===== 读侧：查询（返回查询形状 QO）=====

    List<FlightSearchQO> search(
            @Param("depCity") String depCity,
            @Param("arrCity") String arrCity,
            @Param("depDate") LocalDate depDate,
            @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax,
            @Param("planeId") Long planeId,
            @Param("airportName") String airportName,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /** 满足同一批筛选条件的总条数（分页 total）。 */
    long countSearch(
            @Param("depCity") String depCity,
            @Param("arrCity") String arrCity,
            @Param("depDate") LocalDate depDate,
            @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax,
            @Param("planeId") Long planeId,
            @Param("airportName") String airportName
    );

    // ===== 写侧：聚合读写（返回/接收 PO）=====

    /** 按 id 取一行，给 Repository 加载聚合用。 */
    FlightPO findById(@Param("id") Long id);

    /** 按 id 取一行并加行锁（FOR UPDATE）：写路径"读-改-写"用，防并发删改。必须配合 @Transactional 使用。 */
    FlightPO findByIdForUpdate(@Param("id") Long id);

    /** 改签时校验"同航司"：查某机型所属的航空公司 id（plane.id_airline）。 */
    Long findAirlineIdByPlaneId(@Param("planeId") Long planeId);

    /** 插入一行，自增主键回填到 po.getId()（useGeneratedKeys）。返回受影响行数。 */
    int insert(FlightPO po);

    /** 全字段更新（身份字段写回原值，只有运行字段被领域层改过）。返回受影响行数。 */
    int update(FlightPO po);

    /** 按 id 删除一行。返回受影响行数（0 = 已被并发删除）。 */
    int delete(@Param("id") Long id);
}

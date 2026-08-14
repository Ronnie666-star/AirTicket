package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.FlightPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MyBatis 只存在于基础设施层 —— 整个项目里唯一出现 SQL 的地方。
 * 下划线->驼峰自动映射已开启（application.yml），所以 select 字段不用起别名。
 */
@Mapper
public interface FlightMapper {

    @Select("""
            SELECT id, flight_no, from_city, to_city, depart_time, arrive_time,
                   status, remaining_seats, price_cents
            FROM flight
            WHERE id = #{id}
            """)
    FlightPO findById(@Param("id") Long id);

    @Select("""
            SELECT id, flight_no, from_city, to_city, depart_time, arrive_time,
                   status, remaining_seats, price_cents
            FROM flight
            WHERE status = 1
            ORDER BY depart_time
            """)
    List<FlightPO> findAvailable();

    /**
     * 条件 UPDATE 就是"防超卖"的关键：
     * 只有 status=1（可售）且 remaining_seats >= count（余票够）才真正扣减。
     * 返回影响行数 —— 0 行说明并发下没抢到，业务层据此判定失败。
     */
    @Update("""
            UPDATE flight
            SET remaining_seats = remaining_seats - #{count}
            WHERE id = #{flightId}
              AND status = 1
              AND remaining_seats >= #{count}
            """)
    int tryBook(@Param("flightId") Long flightId, @Param("count") int count);
}

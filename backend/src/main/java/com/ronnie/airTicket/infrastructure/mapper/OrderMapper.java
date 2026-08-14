package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {

    /** useGeneratedKeys 让 MySQL 自增主键回填到 po.id。 */
    @Insert("""
            INSERT INTO orders (order_no, flight_id, passenger_name, passenger_phone, price_cents, status)
            VALUES (#{orderNo}, #{flightId}, #{passengerName}, #{passengerPhone}, #{priceCents}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderPO po);

    @Select("""
            SELECT id, order_no, flight_id, passenger_name, passenger_phone,
                   price_cents, status, created_at, updated_at
            FROM orders
            ORDER BY id DESC
            """)
    List<OrderPO> findAll();
}

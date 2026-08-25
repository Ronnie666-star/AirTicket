package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.query.RouteQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 航班实时轨迹 Mapper（读侧）：按航班查唯一一条轨迹记录。
 */
@Mapper
public interface RouteMapper {

    RouteQO findByFlightId(@Param("flightId") Long flightId);
}

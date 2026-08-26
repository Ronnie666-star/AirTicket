package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.RoutePO;
import com.ronnie.airTicket.infrastructure.persistence.query.RouteQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 航班实时轨迹 Mapper：按航班查唯一一条轨迹记录（读），
 * 以及模拟"机器检测自动更新"的手动编辑入口（写，upsert）。
 */
@Mapper
public interface RouteMapper {

    RouteQO findByFlightId(@Param("flightId") Long flightId);

    /** 插入或更新：每趟航班一条轨迹（idx_route_flight 唯一索引），存在则覆盖，不存在则插入。返回受影响行数。 */
    int upsert(RoutePO po);
}

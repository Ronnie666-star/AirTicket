package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.FlightNotFoundException;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.infrastructure.mapper.RouteMapper;
import com.ronnie.airTicket.infrastructure.persistence.query.RouteQO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 航班实时轨迹用例（读侧模式）：注入 RouteMapper 直接查 QO。
 * 航班不存在 -> 404；无轨迹记录 -> 200 空结果（data 为 null）。
 */
@Service
@RequiredArgsConstructor
public class RouteAppService {

    private final RouteMapper routeMapper;
    private final FlightRepository flightRepository;

    public RouteQueryResult getByFlightId(Long flightId) {
        // 先校验航班存在（不存在 -> 404）
        flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));
        RouteQO qo = routeMapper.findByFlightId(flightId);
        if (qo == null) {
            return null;   // 无轨迹：200 空结果
        }
        return new RouteQueryResult(
                qo.getIdFlight(), qo.getDistanceRemain(), qo.getTimeRemain(),
                qo.getAltitude(), qo.getSpeed(), qo.getLatitude(), qo.getLongitude(), qo.getTimeStamp());
    }
}

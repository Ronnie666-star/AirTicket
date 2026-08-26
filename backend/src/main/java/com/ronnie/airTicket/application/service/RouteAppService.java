package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.FlightNotFoundException;
import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.infrastructure.mapper.RouteMapper;
import com.ronnie.airTicket.infrastructure.persistence.po.RoutePO;
import com.ronnie.airTicket.infrastructure.persistence.query.RouteQO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 航班实时轨迹用例。
 * 读侧：注入 RouteMapper 直接查 QO（航班不存在 -> 404；无轨迹记录 -> 200 空结果）。
 * 写侧：模拟"机器检测自动更新"的手动编辑入口 —— 仅在航班飞行时间窗内可编辑，
 * 且只有该航班的放票者（或管理员）可编辑。
 */
@Service
@RequiredArgsConstructor
public class RouteAppService {

    private final RouteMapper routeMapper;
    private final FlightRepository flightRepository;
    private final FlightAccessGuard flightAccessGuard;

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

    /**
     * 编辑轨迹（模拟机器检测自动更新）：
     * 1) 加锁读航班（防并发删改）；
     * 2) 归属校验：谁放的票谁能编辑（管理员放行）；
     * 3) 飞行时间窗校验：仅 datetime_dep ~ datetime_arr 之间可编辑，否则 400；
     * 4) upsert 轨迹（每趟航班一条）。
     */
    @Transactional
    public RouteQueryResult update(Long flightId, RouteUpdateCommand cmd, CurrentUser user) {
        Flight flight = flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));
        flightAccessGuard.assertCanManage(user, flight);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(flight.getDatetimeDep()) || now.isAfter(flight.getDatetimeArr())) {
            throw new DomainException("仅航班飞行期间可编辑轨迹（当前不在飞行时间窗内）");
        }

        RoutePO po = new RoutePO();
        po.setIdFlight(flightId);
        po.setDistanceRemain(cmd.distanceRemain());
        po.setTimeRemain(cmd.timeRemain());
        po.setAltitude(cmd.altitude());
        po.setSpeed(cmd.speed());
        po.setLatitude(cmd.latitude());
        po.setLongitude(cmd.longitude());
        po.setTimeStamp(cmd.timeStamp());
        routeMapper.upsert(po);

        return new RouteQueryResult(
                flightId, cmd.distanceRemain(), cmd.timeRemain(),
                cmd.altitude(), cmd.speed(), cmd.latitude(), cmd.longitude(), cmd.timeStamp());
    }
}

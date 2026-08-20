package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.FlightNotFoundException;
import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.infrastructure.mapper.FlightMapper;
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSearchQO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 航班用例。
 * 读侧（search）：只读、不加载聚合根，直接注入基础设施层 FlightMapper 查 QO；
 * 写侧（insert / update）：走 domain 的 FlightRepository 加载聚合、改聚合、再存回 —— 依赖倒置落地。
 * 写用例都标 @Transactional：一个用例里的多次 DB 操作要么全成功要么全回滚。
 */
@Service
@RequiredArgsConstructor
public class FlightAppService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    // ===== 读侧：查询不走 Repository =====

    public List<FlightQueryResult> search(FlightQueryCommand cmd) {
        return flightMapper.search(
                cmd.depCity(),
                cmd.arrCity(),
                cmd.depDate(),
                cmd.priceMin(),
                cmd.priceMax(),
                cmd.planeId(),
                cmd.airportName()
        ).stream().map(this::toResult).toList();
    }

    private FlightQueryResult toResult(FlightSearchQO qo) {
        return new FlightQueryResult(
                qo.getId(), qo.getCode(), qo.getDatetimeDep(), qo.getDatetimeArr(),
                qo.getRegionDep(), qo.getRegionArr(), qo.getGate(),
                qo.getDistance(), qo.getPrice(), qo.getStatus()
        );
    }

    // ===== 写侧：走 Repository =====

    /** 创建航班：id 传 null（自增），create_at 交给数据库。 */
    @Transactional
    public FlightDetailResult insert(FlightInsertCommand cmd) {
        Flight flight = new Flight(
                null,
                cmd.idPlane(), cmd.idAirportDep(), cmd.idAirportArr(), cmd.code(),
                cmd.datetimeDep(), cmd.datetimeArr(),
                cmd.regionDep(), cmd.regionArr(), cmd.distance(),
                cmd.seatFirstClass(), cmd.seatBusinessClass(), cmd.seatEconomyClass(),
                cmd.price(), cmd.cancellationFee(), cmd.gate(), cmd.status(),
                null
        );
        flightRepository.save(flight);   // 内部发现 id==null → INSERT，主键回填
        return FlightDetailResult.from(flight);
    }

    /** 更新航班：先按 id 加载聚合（不存在则报错），再改运行字段，最后存回。 */
    @Transactional
    public FlightDetailResult update(Long id, FlightUpdateCommand cmd) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        flight.update(
                cmd.datetimeDep(), cmd.datetimeArr(),
                cmd.seatFirstClass(), cmd.seatBusinessClass(), cmd.seatEconomyClass(),
                cmd.price(), cmd.cancellationFee(), cmd.gate(), cmd.status()
        );
        flightRepository.save(flight);   // 内部发现 id != null → UPDATE
        return FlightDetailResult.from(flight);
    }
}

package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.FlightNotFoundException;
import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.domain.repository.OrderRepository;
import com.ronnie.airTicket.infrastructure.mapper.FlightMapper;
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSearchQO;
import com.ronnie.airTicket.interfaces.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 航班用例。
 * 读侧（search）：只读、不加载聚合根，直接注入基础设施层 FlightMapper 查 QO（分页由 count + LIMIT 完成）；
 * 写侧（insert / update / delete）：走 domain 的 FlightRepository 加载聚合、改聚合、再存回 —— 依赖倒置落地。
 * 写用例都标 @Transactional 且用 findByIdForUpdate 加锁：防并发删改。
 */
@Service
@RequiredArgsConstructor
public class FlightAppService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final OrderRepository orderRepository;

    // ===== 读侧：查询不走 Repository =====

    public PageResult<FlightQueryResult> search(FlightQueryCommand cmd) {
        int page = PageResult.normalizePage(cmd.page());
        int size = PageResult.normalizeSize(cmd.size());
        long total = flightMapper.countSearch(
                cmd.depCity(), cmd.arrCity(), cmd.depDate(),
                cmd.priceMin(), cmd.priceMax(), cmd.planeId(), cmd.airportName());
        List<FlightQueryResult> data = flightMapper.search(
                cmd.depCity(), cmd.arrCity(), cmd.depDate(),
                cmd.priceMin(), cmd.priceMax(), cmd.planeId(), cmd.airportName(),
                (page - 1) * size, size
        ).stream().map(this::toResult).toList();
        return PageResult.of(total, page, size, data);
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

    /**
     * 更新航班：先加锁读（FOR UPDATE）拿聚合，再改运行字段，最后存回。
     * 锁 + 行数校验两层防并发：锁保证"读-改-写"期间没有别的写/删交错；
     * save 返回 false（更新 0 行）说明这行在锁后已被并发删除，直接报"航班不存在"。
     */
    @Transactional
    public FlightDetailResult update(Long id, FlightUpdateCommand cmd) {
        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        flight.update(
                cmd.datetimeDep(), cmd.datetimeArr(),
                cmd.seatFirstClass(), cmd.seatBusinessClass(), cmd.seatEconomyClass(),
                cmd.price(), cmd.cancellationFee(), cmd.gate(), cmd.status()
        );
        if (!flightRepository.save(flight)) {   // 0 行 = 锁后已被并发删除
            throw new FlightNotFoundException(id);
        }
        return FlightDetailResult.from(flight);
    }

    /**
     * 删除航班（数据完整性保护）：先加锁读该行，确认没有订单引用后才删。
     * 锁顺序固定为"先航班后订单"，避免与下单（先锁航班再插订单）形成死锁窗口；
     * 若删除时行已被并发删掉，delete 返回 0 行，报"航班不存在"。
     */
    @Transactional
    public void delete(Long id) {
        Flight flight = flightRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        if (orderRepository.countByFlightId(id) > 0) {
            throw new DomainException("该航班已有订单，无法删除");
        }
        if (!flightRepository.deleteById(id)) {
            throw new FlightNotFoundException(id);
        }
    }
}

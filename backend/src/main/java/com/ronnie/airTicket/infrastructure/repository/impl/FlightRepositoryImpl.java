package com.ronnie.airTicket.infrastructure.repository.impl;

import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.infrastructure.mapper.FlightMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.FlightAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.FlightPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository 接口的 MyBatis 实现。
 * domain 层只认识 FlightRepository 接口，完全看不到这个类的存在 —— 依赖倒置落地。
 * save 是"聚合落库"的入口：id==null 走 INSERT（主键回填），否则走 UPDATE。
 */
@Repository
@RequiredArgsConstructor
public class FlightRepositoryImpl implements FlightRepository {

    private final FlightMapper flightMapper;
    private final FlightAssembler flightAssembler;

    @Override
    public Optional<Flight> findById(Long id) {
        FlightPO po = flightMapper.findById(id);
        return Optional.ofNullable(flightAssembler.toDomain(po));
    }

    @Override
    public void save(Flight flight) {
        FlightPO po = flightAssembler.toPO(flight);
        if (flight.getId() == null) {
            flightMapper.insert(po);          // INSERT，自增主键回填到 po.id
            flight.assignId(po.getId());      // 再回填到领域聚合
        } else {
            flightMapper.update(po);          // UPDATE ... WHERE id
        }
    }
}

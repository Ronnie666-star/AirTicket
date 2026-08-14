package com.ronnie.airTicket.infrastructure.repository.impl;

import com.ronnie.airTicket.domain.model.Flight;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import com.ronnie.airTicket.infrastructure.mapper.FlightMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.FlightAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.FlightPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository 接口的 MyBatis 实现。
 * domain 层只认识 FlightRepository 接口，完全看不到这个类的存在 —— 这就是依赖倒置落地。
 */
@Repository
@RequiredArgsConstructor
public class FlightRepositoryImpl implements FlightRepository {

    private final FlightMapper flightMapper;
    private final FlightAssembler flightAssembler;

    @Override
    public Flight findById(Long id) {
        FlightPO po = flightMapper.findById(id);
        return flightAssembler.toDomain(po);
    }

    @Override
    public List<Flight> findAvailable() {
        return flightMapper.findAvailable().stream()
                .map(flightAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean tryBook(Long flightId, int count) {
        return flightMapper.tryBook(flightId, count) == 1;
    }
}

package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.model.Flight;
import com.ronnie.airTicket.domain.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 读侧用例：查可售航班。只做查询编排，不写业务规则。
 * 简单查询直接复用 domain 仓库；报表类复杂查询再考虑 CQRS 读侧模型。
 */
@Service
@RequiredArgsConstructor
public class FlightQueryAppService {

    private final FlightRepository flightRepository;

    public List<Flight> listAvailable() {
        return flightRepository.findAvailable();
    }
}

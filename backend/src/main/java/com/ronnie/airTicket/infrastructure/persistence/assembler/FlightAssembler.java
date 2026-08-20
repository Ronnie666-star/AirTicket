package com.ronnie.airTicket.infrastructure.persistence.assembler;

import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.infrastructure.persistence.po.FlightPO;
import org.springframework.stereotype.Component;

/**
 * PO <-> domain 转换。两个方向都要：
 *   toDomain：查出来的一行 -> 领域聚合（写侧用，如 findById）；
 *   toPO：聚合 -> 一行（insert / update 用）。
 */
@Component
public class FlightAssembler {

    public Flight toDomain(FlightPO po) {
        if (po == null) {
            return null;
        }
        return new Flight(
                po.getId(),
                po.getIdPlane(), po.getIdAirportDep(), po.getIdAirportArr(), po.getCode(),
                po.getDatetimeDep(), po.getDatetimeArr(),
                po.getRegionDep(), po.getRegionArr(), po.getDistance(),
                po.getSeatFirstClass(), po.getSeatBusinessClass(), po.getSeatEconomyClass(),
                po.getPrice(), po.getCancellationFee(), po.getGate(), po.getStatus(),
                po.getCreateAt()
        );
    }

    public FlightPO toPO(Flight flight) {
        FlightPO po = new FlightPO();
        po.setId(flight.getId());
        po.setIdPlane(flight.getIdPlane());
        po.setIdAirportDep(flight.getIdAirportDep());
        po.setIdAirportArr(flight.getIdAirportArr());
        po.setCode(flight.getCode());
        po.setDatetimeDep(flight.getDatetimeDep());
        po.setDatetimeArr(flight.getDatetimeArr());
        po.setRegionDep(flight.getRegionDep());
        po.setRegionArr(flight.getRegionArr());
        po.setDistance(flight.getDistance());
        po.setSeatFirstClass(flight.getSeatFirstClass());
        po.setSeatBusinessClass(flight.getSeatBusinessClass());
        po.setSeatEconomyClass(flight.getSeatEconomyClass());
        po.setPrice(flight.getPrice());
        po.setCancellationFee(flight.getCancellationFee());
        po.setGate(flight.getGate());
        po.setStatus(flight.getStatus());
        po.setCreateAt(flight.getCreateAt());
        return po;
    }
}

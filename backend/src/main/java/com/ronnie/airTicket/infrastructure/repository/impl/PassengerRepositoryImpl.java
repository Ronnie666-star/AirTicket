package com.ronnie.airTicket.infrastructure.repository.impl;

import com.ronnie.airTicket.domain.model.reference.Passenger;
import com.ronnie.airTicket.domain.repository.PassengerRepository;
import com.ronnie.airTicket.infrastructure.mapper.PassengerMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.PassengerAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.PassengerPO;
import com.ronnie.airTicket.infrastructure.persistence.query.PassengerQO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PassengerRepository 的 MyBatis 实现。
 * 乘客表是纯关系表，没有领域行为，Repository 操作 Mapper 的 PO/QO 并转成领域值对象。
 */
@Repository
@RequiredArgsConstructor
public class PassengerRepositoryImpl implements PassengerRepository {

    private final PassengerMapper passengerMapper;
    private final PassengerAssembler passengerAssembler;

    @Override
    public List<Passenger> listByUserId(Long userId) {
        return passengerMapper.selectByUserId(userId).stream()
                .map(passengerAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<Passenger> findById(Long id) {
        return Optional.ofNullable(passengerAssembler.toDomain(passengerMapper.selectById(id)));
    }

    @Override
    public boolean existsByUserIdAndPassengerId(Long userId, Long passengerId) {
        return passengerMapper.countByUserIdAndPassengerId(userId, passengerId) > 0;
    }

    @Override
    public Passenger add(Long userId, Long passengerId) {
        PassengerPO po = passengerAssembler.toPO(userId, passengerId);
        if (passengerMapper.insert(po) == 0) {
            return null;   // 撞唯一约束（并发重复）
        }
        return new Passenger(po.getId(), userId, passengerId, null, null, null);
    }

    @Override
    public boolean deleteById(Long id) {
        return passengerMapper.deleteById(id) > 0;
    }
}

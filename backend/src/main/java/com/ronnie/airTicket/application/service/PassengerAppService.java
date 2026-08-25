package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.model.reference.Passenger;
import com.ronnie.airTicket.domain.repository.PassengerRepository;
import com.ronnie.airTicket.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 常用乘机人用例：旅客维护自己账号下的常用乘机人（增删查）。
 * 校验：目标用户不存在 -> 400；重复添加 -> 400（并发撞唯一索引由 DuplicateKeyException -> 409 兜底）；
 * 删除不存在 / 他人记录 -> 400。
 */
@Service
@RequiredArgsConstructor
public class PassengerAppService {

    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;

    /** 我的常用乘机人列表（只含本人添加的）。 */
    public List<PassengerResult> list(Long userId) {
        return passengerRepository.listByUserId(userId).stream()
                .map(PassengerResult::from)
                .toList();
    }

    /** 添加常用乘机人：passengerId 指向一个存在的系统用户。 */
    @Transactional
    public PassengerResult add(Long userId, Long passengerId) {
        // 目标用户必须存在
        if (userRepository.findById(passengerId).isEmpty()) {
            throw new DomainException("目标用户不存在");
        }
        // 重复添加预判
        if (passengerRepository.existsByUserIdAndPassengerId(userId, passengerId)) {
            throw new DomainException("该乘机人已添加");
        }
        Passenger saved = passengerRepository.add(userId, passengerId);
        if (saved == null) {
            throw new DomainException("该乘机人已添加");   // 并发撞唯一索引兜底
        }
        // 回读乘机人姓名 / 用户名（JOIN sys_user），带进响应
        return passengerRepository.findById(saved.id())
                .map(PassengerResult::from)
                .orElseGet(() -> PassengerResult.from(saved));
    }

    /** 删除常用乘机人：只能删自己的记录；不存在 / 他人记录 -> 400。 */
    @Transactional
    public void delete(Long userId, Long id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new DomainException("记录不存在"));
        if (!passenger.userId().equals(userId)) {
            throw new DomainException("记录不存在");
        }
        passengerRepository.deleteById(id);
    }
}

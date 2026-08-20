package com.ronnie.airTicket.infrastructure.repository.impl;

import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.repository.UserRepository;
import com.ronnie.airTicket.infrastructure.mapper.SysUserMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.SysUserAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.SysUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository 接口的 MyBatis 实现。
 * domain 层只认识 UserRepository 接口，完全看不到这个类的存在 —— 这就是依赖倒置落地。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper sysUserMapper;
    private final SysUserAssembler sysUserAssembler;

    @Override
    public Optional<User> findByUsername(String username) {
        SysUserPO po = sysUserMapper.findByUsername(username);
        return Optional.ofNullable(sysUserAssembler.toDomain(po));
    }
}

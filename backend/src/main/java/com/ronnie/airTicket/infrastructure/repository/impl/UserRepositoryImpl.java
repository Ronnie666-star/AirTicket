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
 * save 是"聚合落库"的入口：id==null 走 INSERT（主键回填），否则走 UPDATE。
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

    @Override
    public Optional<User> findById(Long id) {
        SysUserPO po = sysUserMapper.findById(id);
        return Optional.ofNullable(sysUserAssembler.toDomain(po));
    }

    @Override
    public long count() {
        return sysUserMapper.count();
    }

    @Override
    public boolean save(User user) {
        SysUserPO po = sysUserAssembler.toPO(user);
        if (user.getId() == null) {
            sysUserMapper.insert(po);         // INSERT，自增主键回填到 po.id
            user.assignId(po.getId());        // 再回填到领域聚合
            return true;
        }
        return sysUserMapper.update(po) > 0;  // 0 行 = 这行已被并发删除
    }
}

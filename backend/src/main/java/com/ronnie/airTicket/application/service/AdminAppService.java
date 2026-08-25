package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.UserNotFoundException;
import com.ronnie.airTicket.domain.exception.UsernameTakenException;
import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.domain.repository.UserRepository;
import com.ronnie.airTicket.domain.service.PasswordHasher;
import com.ronnie.airTicket.domain.service.PasswordPolicy;
import com.ronnie.airTicket.infrastructure.mapper.SysUserMapper;
import com.ronnie.airTicket.infrastructure.persistence.assembler.SysUserAssembler;
import com.ronnie.airTicket.infrastructure.persistence.po.SysUserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员用户管理用例（写侧走 Repository，读侧走 Mapper）。
 * 接口层已用 @RequireRole(ADMIN) 挡住非管理员，这里只做业务规则：
 *   列表筛选（username/role/enabled）、创建商家（MERCHANT 角色、初始密码管理员设置）、
 *   启用/禁用（不能操作自己）、重置密码（强度校验）。
 */
@Service
@RequiredArgsConstructor
public class AdminAppService {

    private final UserRepository userRepository;
    private final SysUserMapper sysUserMapper;
    private final SysUserAssembler sysUserAssembler;
    private final PasswordHasher passwordHasher;

    /** 用户列表：读侧直接走 Mapper，响应不含密码。 */
    public List<AdminUserResult> list(String username, String role, Boolean enabled) {
        List<SysUserPO> pos = sysUserMapper.search(username, role, enabled);
        return pos.stream()
                .map(sysUserAssembler::toDomain)
                .map(AdminUserResult::from)
                .toList();
    }

    /**
     * 创建商家（管理员分配）：固定 MERCHANT 角色、启用状态，初始密码由管理员设置。
     * 校验：用户名非空且未占用、密码强度、联系方式至少一个且格式合法；
     * 真实姓名 / 年龄商家可留空（User.create 按角色校验）。
     */
    @Transactional
    public AdminUserResult createMerchant(String username, String rawPassword, String realName,
                                          Integer age, String email, String phone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameTakenException();
        }
        PasswordPolicy.assertValid(rawPassword);
        User merchant = User.create(
                null, username, passwordHasher.hash(rawPassword), realName, age, email, phone,
                UserRole.MERCHANT, null);
        userRepository.save(merchant);
        return AdminUserResult.from(merchant);
    }

    /** 启用 / 禁用账号：目标为当前登录管理员 -> 400（防自杀）。 */
    @Transactional
    public void changeStatus(Long operatorId, Long userId, boolean enabled) {
        if (operatorId.equals(userId)) {
            throw new DomainException("不能操作自己的账号");
        }
        User user = load(userId);
        user.changeEnabled(enabled);
        userRepository.save(user);
    }

    /** 重置密码：新密码过 PasswordPolicy -> BCrypt 重哈希。 */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        PasswordPolicy.assertValid(newPassword);
        User user = load(userId);
        user.changePassword(passwordHasher.hash(newPassword));
        userRepository.save(user);
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}

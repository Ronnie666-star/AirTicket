package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.UsernameTakenException;
import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.domain.repository.UserRepository;
import com.ronnie.airTicket.domain.service.PasswordHasher;
import com.ronnie.airTicket.domain.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 初始化用例：在系统尚无任何用户时创建初始管理员（唯一入口，只允许一次）。
 * 安全模型：不依赖登录（此时还没有管理员），靠"只能初始化一次"保证 ——
 * 系统已有任一用户（count>0）时调用返回 400，只有空库能创建。删库重建后首次调用即创建。
 * 校验规则：用户名非空、密码强度、联系方式至少一个且格式合法；
 * 真实姓名 / 年龄管理员可留空（User.create 按角色校验）。
 */
@Service
@RequiredArgsConstructor
public class InitAppService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    /** 系统是否已初始化（是否已存在任何用户）：供前端决定是否显示初始化向导。 */
    public boolean isInitialized() {
        return userRepository.count() > 0;
    }

    @Transactional
    public InitAdminResult createAdmin(String username, String rawPassword, String realName,
                                       Integer age, String email, String phone) {
        // ① 只允许一次：系统已有任何用户 -> 400（不能重复初始化）
        if (userRepository.count() > 0) {
            throw new DomainException("系统已初始化，无法再次创建初始管理员");
        }
        // ② 密码强度
        PasswordPolicy.assertValid(rawPassword);
        // ③ 创建管理员（User.create 内校验：密码强度 / 真实姓名·年龄按角色 / 联系方式）
        User admin = User.create(
                null, username, passwordHasher.hash(rawPassword), realName, age, email, phone,
                UserRole.ADMIN, null);
        userRepository.save(admin);
        return InitAdminResult.from(admin);
    }
}

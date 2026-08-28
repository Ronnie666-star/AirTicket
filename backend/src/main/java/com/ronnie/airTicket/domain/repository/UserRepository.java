package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.user.User;
import com.ronnie.airTicket.domain.model.user.UserRole;

import java.util.Optional;

/**
 * 用户仓储端口（Port）。domain 层只声明"我要找到用户 / 存用户"，
 * MyBatis 怎么查怎么写是基础设施层的事 —— 依赖倒置让领域逻辑不依赖数据库。
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    /** 系统用户总数：初始化端点判断"系统是否已存在用户"用。 */
    long count();

    /** 某角色用户数：初始化端点判断"系统是否已存在管理员"用（V6 内置旅客/商家演示数据后，仍允许创建初始管理员）。 */
    long countByRole(UserRole role);

    /**
     * 保存聚合：内部判断 id==null 走 insert（主键回填），否则走 update。
     * @return 是否真正改了行。update 没碰到任何行（0 行，已被并发删除）返回 false。
     */
    boolean save(User user);
}

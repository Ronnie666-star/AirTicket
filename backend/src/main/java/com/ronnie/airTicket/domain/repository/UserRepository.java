package com.ronnie.airTicket.domain.repository;

import com.ronnie.airTicket.domain.model.user.User;

import java.util.Optional;

/**
 * 用户仓储端口（Port）。domain 层只声明"我要按用户名找到用户"，
 * MyBatis 怎么查是基础设施层的事 —— 依赖倒置让领域逻辑不依赖数据库。
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);
}

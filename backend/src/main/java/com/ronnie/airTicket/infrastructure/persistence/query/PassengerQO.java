package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询对象（QO）：给"我的常用乘机人"查询用的结果形状。
 * 除 passenger 表本身列，还 JOIN sys_user 带出乘机人的真实姓名 / 用户名（展示用，不含密码）。
 */
@Data
public class PassengerQO {

    private Long id;
    private Long userId;
    private Long passengerId;
    private String realName;
    private String username;
    private LocalDateTime createAt;
}

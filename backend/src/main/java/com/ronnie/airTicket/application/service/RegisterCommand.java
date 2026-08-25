package com.ronnie.airTicket.application.service;

/** 注册用例的输入：controller 把请求体转成命令对象交给应用服务。 */
public record RegisterCommand(
        String username,
        String rawPassword,
        String realName,
        Integer age,
        String email,
        String phone
) {
}

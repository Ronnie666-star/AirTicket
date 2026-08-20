package com.ronnie.airTicket.application.service;

/** 登录用例的输入：controller 把请求体转成命令对象交给应用服务。 */
public record LoginCommand(String username, String rawPassword) {
}

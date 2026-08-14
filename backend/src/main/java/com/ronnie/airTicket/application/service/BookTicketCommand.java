package com.ronnie.airTicket.application.service;

/**
 * 应用层命令对象：描述"一次购票用例需要哪些输入"。
 * 用 record 表达不可变输入，interface 层把 HTTP 参数组装成它。
 */
public record BookTicketCommand(Long flightId, String passengerName, String passengerPhone) {
}

package com.ronnie.airTicket.application.service;

/** 改签用例的输入：要改签去的新航班。旧订单 id 走 URL 路径。 */
public record OrderRescheduleCommand(
        Long newFlightId
) {
}

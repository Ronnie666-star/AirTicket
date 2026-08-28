package com.ronnie.airTicket.interfaces.controller;

import com.ronnie.airTicket.application.service.StatsAppService;
import com.ronnie.airTicket.domain.model.user.UserRole;
import com.ronnie.airTicket.infrastructure.persistence.query.ChannelRevenueQO;
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSalesQO;
import com.ronnie.airTicket.infrastructure.persistence.query.RevenueOverviewQO;
import com.ronnie.airTicket.infrastructure.persistence.query.TopPassengerQO;
import com.ronnie.airTicket.interfaces.common.ApiResponse;
import com.ronnie.airTicket.interfaces.common.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员统计接口：全部 @RequireRole(ADMIN)（拦截器 + 后端双保险），前缀 /admin/** 已注册 AuthInterceptor。
 * 3 个统计功能（对应课程设计要求"至少 3 个统计功能"）：
 *   GET /admin/stats/flight-sales    统计① 热门航班销量 Top（订单数 / 成交金额 / 座舱利用率）
 *   GET /admin/stats/revenue         统计②a 营收总览（成交总额 / 实收 / 退款 / 退票费收入 / 单量）
 *   GET /admin/stats/revenue/channels统计②b 渠道营收占比
 *   GET /admin/stats/top-passengers  统计③ 旅客消费排行 Top
 * 说明：统计为纯只读聚合，无敏感字段，直接返回基础设施层查询形状（QO），不再包一层 DTO。
 */
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsAppService statsAppService;

    @GetMapping("/flight-sales")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<List<FlightSalesQO>> flightSales(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(statsAppService.topFlightSales(limit));
    }

    @GetMapping("/revenue")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<RevenueOverviewQO> revenue() {
        return ApiResponse.ok(statsAppService.revenueOverview());
    }

    @GetMapping("/revenue/channels")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<List<ChannelRevenueQO>> channels() {
        return ApiResponse.ok(statsAppService.channelRevenue());
    }

    @GetMapping("/top-passengers")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<List<TopPassengerQO>> topPassengers(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(statsAppService.topPassengers(limit));
    }
}

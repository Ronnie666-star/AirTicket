package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.infrastructure.mapper.StatsMapper;
import com.ronnie.airTicket.infrastructure.persistence.query.ChannelRevenueQO;
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSalesQO;
import com.ronnie.airTicket.infrastructure.persistence.query.RevenueOverviewQO;
import com.ronnie.airTicket.infrastructure.persistence.query.TopPassengerQO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 统计用例（纯读侧聚合查询）：支撑管理后台"数据统计"页的 3 个统计功能。
 * 读侧不走聚合根，直接注入基础设施层 StatsMapper 查 QO —— 与 FlightAppService.search 同一模式。
 * 接口层已用 @RequireRole(ADMIN) 挡住非管理员，这里只做查询编排与参数兜底。
 * 口径约定（与 V6 演示数据对齐，讲解时保持一致）：
 *   成交金额 = 已支付（PAID）/ 已退款（REFUNDED）订单金额之和（含已退但成交过的）；
 *   实收营收 = 仅已支付（PAID）；退款总额 = 仅已退款（REFUNDED）。
 */
@Service
@RequiredArgsConstructor
public class StatsAppService {

    private static final int DEFAULT_TOP_N = 10;
    private static final int MAX_TOP_N = 50;

    private final StatsMapper statsMapper;

    /** 统计①：热门航班销量 Top（默认 10，最多 50）。 */
    public List<FlightSalesQO> topFlightSales(int limit) {
        return statsMapper.topFlightSales(normalize(limit));
    }

    /** 统计②a：营收总览（单行汇总）。 */
    public RevenueOverviewQO revenueOverview() {
        return statsMapper.revenueOverview();
    }

    /** 统计②b：渠道营收占比。 */
    public List<ChannelRevenueQO> channelRevenue() {
        return statsMapper.channelRevenue();
    }

    /** 统计③：旅客消费排行 Top（默认 10，最多 50）。 */
    public List<TopPassengerQO> topPassengers(int limit) {
        return statsMapper.topPassengers(normalize(limit));
    }

    private int normalize(int limit) {
        if (limit <= 0) {
            return DEFAULT_TOP_N;
        }
        return Math.min(limit, MAX_TOP_N);
    }
}

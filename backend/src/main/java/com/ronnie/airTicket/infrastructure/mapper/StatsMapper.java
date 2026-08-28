package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.query.ChannelRevenueQO;
import com.ronnie.airTicket.infrastructure.persistence.query.FlightSalesQO;
import com.ronnie.airTicket.infrastructure.persistence.query.RevenueOverviewQO;
import com.ronnie.airTicket.infrastructure.persistence.query.TopPassengerQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计 Mapper：只存在于基础设施层，是统计 SQL 唯一出现的地方（读侧聚合查询，不走聚合根）。
 * 供管理员"数据统计"页展示 3 个统计功能：
 *   1) 热门航班销量 Top（航班 × 已售座位 / 成交金额 / 利用率）
 *   2) 营收总览 + 渠道营收占比
 *   3) 旅客消费排行 Top
 */
@Mapper
public interface StatsMapper {

    /** 热门航班销量 Top：按非取消订单数降序，取前 limit 趟航班。 */
    List<FlightSalesQO> topFlightSales(@Param("limit") int limit);

    /** 营收总览：整表聚合，返回单行。 */
    RevenueOverviewQO revenueOverview();

    /** 渠道营收占比：按渠道分组。 */
    List<ChannelRevenueQO> channelRevenue();

    /** 旅客消费排行 Top：按成交金额降序，取前 limit 位旅客（限已支付/已退款订单）。 */
    List<TopPassengerQO> topPassengers(@Param("limit") int limit);
}

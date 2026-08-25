package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.ChannelPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 渠道 Mapper：CRUD + 引用计数（被订单 orders.id_channel 引用）。 */
@Mapper
public interface ChannelMapper {

    List<ChannelPO> list();

    ChannelPO findById(@Param("id") Long id);

    /** 下单默认渠道：按渠道名查回 id（启动自愈确保"官方网站"存在）。 */
    ChannelPO findByName(@Param("channelName") String channelName);

    int insert(ChannelPO po);

    /** 启动自愈用：按渠道名不存在则插入（name 唯一索引保证并发安全），主键回填到 po.id。 */
    int insertIfAbsentByName(ChannelPO po);

    int update(ChannelPO po);

    int delete(@Param("id") Long id);

    /** 删除前引用保护：该渠道被多少订单引用。 */
    long countByChannelId(@Param("channelId") Long channelId);
}

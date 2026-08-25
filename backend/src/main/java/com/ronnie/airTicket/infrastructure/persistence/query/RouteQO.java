package com.ronnie.airTicket.infrastructure.persistence.query;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 查询对象（QO）：给"航班实时轨迹"这一个查询用的结果形状。
 * 字段即页面要展示的内容：剩余距离 / 剩余时间 / 高度 / 速度 / 纬度 / 经度 / 采集时间。
 */
@Data
public class RouteQO {

    private Long id;
    private Long idFlight;
    private Integer distanceRemain;
    private Integer timeRemain;
    private BigDecimal altitude;
    private BigDecimal speed;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime timeStamp;
    private LocalDateTime createAt;
}

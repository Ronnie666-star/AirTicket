package com.ronnie.airTicket.domain.model.flight;

/**
 * 舱级，对齐 orders.cabin_class 列（存枚举名，VARCHAR(20)）。
 * 舱级是"航班的座位分级"概念，归属 flight 领域；Order 引用它记录"订的是哪舱"。
 * 注意：V3 迁移把既有行的默认值回填为 'ECONOMY'（非枚举名），
 * 数据库 'ECONOMY' 与枚举 {@link #ECONOMY_CLASS} 等价，Assembler 负责归一化。
 */
public enum CabinClass {

    FIRST_CLASS,     // 头等舱
    BUSINESS_CLASS,  // 商务舱
    ECONOMY_CLASS    // 经济舱
}

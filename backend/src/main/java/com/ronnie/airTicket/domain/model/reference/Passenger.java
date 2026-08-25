package com.ronnie.airTicket.domain.model.reference;

import java.time.LocalDateTime;

/**
 * 常用乘机人（值对象，非聚合根）：乘客表是纯关系表，没有领域行为。
 * passengerId 指向 sys_user.id（乘机人本身也是系统用户），realName/username 由 JOIN 带出用于展示。
 */
public record Passenger(
        Long id,
        Long userId,        // 添加者（本人）
        Long passengerId,   // 被添加的乘机人（sys_user.id）
        String realName,
        String username,
        LocalDateTime createAt
) {
}

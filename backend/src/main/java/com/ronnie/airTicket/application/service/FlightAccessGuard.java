package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.ForbiddenException;
import com.ronnie.airTicket.domain.model.flight.Flight;
import com.ronnie.airTicket.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 航班访问控制：谁放的票谁能编辑其航班信息和 route 信息。
 * 规则：
 *   管理员 -> 可管理一切航班（放行）；
 *   商家   -> 只能管理自己放出的航班（createdBy 必须等于自己）；
 *   其它角色 / 归属不符 -> 抛 403。
 * 被 FlightAppService（改/删航班）和 RouteAppService（编辑轨迹）共用。
 */
@Component
@RequiredArgsConstructor
public class FlightAccessGuard {

    public void assertCanManage(CurrentUser user, Flight flight) {
        if (user.role() == UserRole.ADMIN) {
            return;
        }
        if (user.role() != UserRole.MERCHANT) {
            throw new ForbiddenException("无权限执行此操作");
        }
        if (flight.getCreatedBy() == null || !flight.getCreatedBy().equals(user.userId())) {
            throw new ForbiddenException("只能编辑自己放出的航班");
        }
    }
}

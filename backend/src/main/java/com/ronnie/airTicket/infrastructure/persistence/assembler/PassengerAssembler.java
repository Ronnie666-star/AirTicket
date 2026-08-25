package com.ronnie.airTicket.infrastructure.persistence.assembler;

import com.ronnie.airTicket.domain.model.reference.Passenger;
import com.ronnie.airTicket.infrastructure.persistence.po.PassengerPO;
import com.ronnie.airTicket.infrastructure.persistence.query.PassengerQO;
import org.springframework.stereotype.Component;

/**
 * 常用乘机人的 QO/PO <-> domain 值对象转换。
 * 读侧：PassengerQO（JOIN 带姓名/用户名）-> Passenger；
 * 写侧：userId + passengerId -> PassengerPO（插入一行）。
 */
@Component
public class PassengerAssembler {

    public Passenger toDomain(PassengerQO qo) {
        if (qo == null) {
            return null;
        }
        return new Passenger(
                qo.getId(), qo.getUserId(), qo.getPassengerId(),
                qo.getRealName(), qo.getUsername(), qo.getCreateAt());
    }

    public PassengerPO toPO(Long userId, Long passengerId) {
        PassengerPO po = new PassengerPO();
        po.setUserId(userId);
        po.setPassengerId(passengerId);
        return po;
    }
}

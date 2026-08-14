package com.ronnie.airTicket.infrastructure.persistence.assembler;

import com.ronnie.airTicket.domain.model.Flight;
import com.ronnie.airTicket.domain.model.FlightStatus;
import com.ronnie.airTicket.domain.model.Money;
import com.ronnie.airTicket.infrastructure.persistence.po.FlightPO;
import org.springframework.stereotype.Component;

/**
 * domain <-> PO 转换。正式项目常用 MapStruct 自动生成，这里手写，方便看清映射逻辑。
 * 注意 PO 和 domain 的字段类型不同（status 是 Integer，domain 是 FlightStatus；
 * price_cents 是 Long，domain 是 Money）——这就是"双模型"存在的原因。
 */
@Component
public class FlightAssembler {

    public Flight toDomain(FlightPO po) {
        if (po == null) {
            return null;
        }
        return new Flight(
                po.getId(),
                po.getFlightNo(),
                po.getFromCity(),
                po.getToCity(),
                po.getDepartTime(),
                po.getArriveTime(),
                FlightStatus.of(po.getStatus()),
                po.getRemainingSeats(),
                Money.ofCents(po.getPriceCents())
        );
    }
}

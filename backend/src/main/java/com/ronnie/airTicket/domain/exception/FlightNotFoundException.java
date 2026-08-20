package com.ronnie.airTicket.domain.exception;

/** 航班不存在：更新航班时按 id 找不到目标。 */
public class FlightNotFoundException extends DomainException {

    public FlightNotFoundException(Long id) {
        super("航班不存在：id=" + id);
    }
}

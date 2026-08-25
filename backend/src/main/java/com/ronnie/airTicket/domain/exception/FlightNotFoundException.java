package com.ronnie.airTicket.domain.exception;

/** 航班不存在：按 id 找不到目标。404。 */
public class FlightNotFoundException extends ResourceNotFoundException {

    public FlightNotFoundException(Long id) {
        super("航班不存在：id=" + id);
    }
}

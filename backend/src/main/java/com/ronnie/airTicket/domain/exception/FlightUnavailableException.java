package com.ronnie.airTicket.domain.exception;

/** 航班不存在 / 状态不可售时抛出。 */
public class FlightUnavailableException extends DomainException {

    public FlightUnavailableException(String message) {
        super(message);
    }
}

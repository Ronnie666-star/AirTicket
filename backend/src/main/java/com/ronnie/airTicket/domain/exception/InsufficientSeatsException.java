package com.ronnie.airTicket.domain.exception;

/** 余票不足时抛出。 */
public class InsufficientSeatsException extends DomainException {

    public InsufficientSeatsException(String message) {
        super(message);
    }
}

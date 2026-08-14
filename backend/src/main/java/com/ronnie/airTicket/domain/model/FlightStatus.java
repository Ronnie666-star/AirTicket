package com.ronnie.airTicket.domain.model;

/** 航班状态，对齐 flight.status 列（1 可售 2 售罄 3 取消）。 */
public enum FlightStatus {

    AVAILABLE(1),
    SOLD_OUT(2),
    CANCELLED(3);

    private final int code;

    FlightStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static FlightStatus of(int code) {
        for (FlightStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知航班状态: " + code);
    }
}

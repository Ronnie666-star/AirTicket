package com.ronnie.airTicket.domain.model;

import java.math.BigDecimal;

/**
 * 值对象：金额。
 * 特征：不可变、无身份、靠"值相等"比较（equals/hashCode）。
 * 约定：金额一律以"分"为存储单位（long），禁止 double/float —— 浮点算钱会出错。
 */
public final class Money {

    private final long cents;

    private Money(long cents) {
        this.cents = cents;
    }

    /** 从"元"创建：12.50 元 -> 1250 分 */
    public static Money ofYuan(BigDecimal yuan) {
        return new Money(yuan.movePointRight(2).longValueExact());
    }

    /** 从"分"创建 */
    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public long cents() {
        return cents;
    }

    /** 转回"元"，仅用于展示 */
    public BigDecimal yuan() {
        return BigDecimal.valueOf(cents, 2);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money && ((Money) o).cents == cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }

    @Override
    public String toString() {
        return "Money(" + cents + "分)";
    }
}

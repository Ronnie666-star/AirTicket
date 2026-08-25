package com.ronnie.airTicket.domain.service;

import com.ronnie.airTicket.domain.exception.DomainException;

/**
 * 密码强度策略（domain service，同 PasswordHasher 风格）。
 * 规则：无长度限制，大小写字母 / 数字 / 符号至少含三类。
 * 登录不校验（沿用库哈希），注册 / 改密 / 管理员重置共用本策略。
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    /** 校验密码强度，不满足抛 DomainException(400)。 */
    public static void assertValid(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("密码不能为空");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        for (char c : rawPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }
        int kinds = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSymbol ? 1 : 0);
        if (kinds < 3) {
            throw new DomainException("密码强度不足：需包含大小写字母、数字、符号中至少三类");
        }
    }
}

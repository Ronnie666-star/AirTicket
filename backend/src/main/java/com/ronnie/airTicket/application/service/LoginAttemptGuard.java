package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录防暴力破解（进程内实现）。
 * 对同一用户名记录连续失败次数：达到上限锁 5 分钟。
 * 说明：生产上更稳妥的是再叠加按 IP 限流、或把计数放到 Redis（多实例共享）；
 * 单实例课程项目用进程内 ConcurrentHashMap 足够。
 */
@Slf4j
@Component
public class LoginAttemptGuard {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    /** 若该用户名已处于锁定，抛 401，不再校验密码。 */
    public void assertNotLocked(String username) {
        Entry e = store.get(username);
        if (e != null && e.failureCount >= MAX_FAILURES && e.lockedUntil.isAfter(Instant.now())) {
            throw new AuthenticationException("登录尝试次数过多，请稍后再试");
        }
    }

    /** 记一次失败：连续失败达到上限则锁定 5 分钟。 */
    public void recordFailure(String username) {
        store.compute(username, (key, cur) -> {
            Entry base = (cur == null) ? new Entry(0, null) : cur;
            // 已锁定期间不再累计（否则每次失败都会刷新锁定窗口）
            if (base.failureCount >= MAX_FAILURES && base.lockedUntil.isAfter(Instant.now())) {
                return base;
            }
            if (base.failureCount + 1 >= MAX_FAILURES) {
                log.warn("用户名 {} 连续登录失败达到上限，锁定 {} 分钟", username, LOCK_DURATION.toMinutes());
                return new Entry(MAX_FAILURES, Instant.now().plus(LOCK_DURATION));
            }
            return new Entry(base.failureCount + 1, null);
        });
    }

    /** 登录成功清除计数。 */
    public void reset(String username) {
        store.remove(username);
    }

    private static final class Entry {
        final int failureCount;
        final Instant lockedUntil;

        Entry(int failureCount, Instant lockedUntil) {
            this.failureCount = failureCount;
            this.lockedUntil = lockedUntil;
        }
    }
}

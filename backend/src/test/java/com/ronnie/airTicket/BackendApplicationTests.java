package com.ronnie.airTicket;

import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * 上下文冒烟：加载 .env（JWT_SECRET 等敏感配置来自 backend/.env），拉起完整 Spring 上下文。
 */
@SpringBootTest
@ContextConfiguration(initializers = DotenvApplicationInitializer.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}

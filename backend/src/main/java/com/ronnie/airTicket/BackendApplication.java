package com.ronnie.airTicket;

import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyBatis mapper 接口都放在 infrastructure.mapper，用 @MapperScan 统一注册。
 * 领域 / 应用 / 接口层不允许出现任何 MyBatis 注解。
 */
@MapperScan("com.ronnie.airTicket.infrastructure.mapper")
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        // 本地启动时把 backend/.env 里的键值注入 Spring 环境；
        // Docker 场景不需要它（镜像内没有 .env），由容器注入真实环境变量。
        app.addInitializers(new DotenvApplicationInitializer());
        app.run(args);
    }

}

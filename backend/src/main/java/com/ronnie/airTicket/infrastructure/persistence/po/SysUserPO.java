package com.ronnie.airTicket.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 持久化对象（PO）：对齐 sys_user 表的每一列，是 MyBatis 的结果映射目标。
 * 它跟 domain 的 User 是两套模型 —— PO 跟着数据库走，domain 跟着业务走。
 */
@Data
public class SysUserPO {

    private Long id;
    private String username;
    private String password;      // 库里存的是 BCrypt 哈希
    private String realName;
    private Integer age;
    private String email;
    private String phone;
    private Boolean status;       // 列是 BOOLEAN/TINYINT(1)
    private String role;          // 列是 VARCHAR，存枚举名
    private LocalDateTime createAt;
}

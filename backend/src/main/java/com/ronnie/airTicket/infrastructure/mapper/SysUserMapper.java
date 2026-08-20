package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.SysUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MyBatis 只存在于基础设施层 —— 整个项目里唯一出现 SQL 的地方。
 * 下划线->驼峰自动映射已开启（application.yml），所以 select 字段不用起别名。
 */
@Mapper
public interface SysUserMapper {

    @Select("""
            SELECT id, username, password, real_name, age, email, phone, status, role, create_at
            FROM sys_user
            WHERE username = #{username}
            """)
    SysUserPO findByUsername(@Param("username") String username);
}

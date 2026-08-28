package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.SysUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis 只存在于基础设施层 —— 整个项目里唯一出现 SQL 的地方。
 * 下划线->驼峰自动映射已开启（application.yml），所以 select 字段不用起别名。
 */
@Mapper
public interface SysUserMapper {

    SysUserPO findByUsername(@Param("username") String username);

    SysUserPO findById(@Param("id") Long id);

    /** 插入一行，自增主键回填到 po.getId()（useGeneratedKeys）。返回受影响行数。 */
    int insert(SysUserPO po);

    /** 全字段更新。返回受影响行数（0 = 记录已被并发删除）。 */
    int update(SysUserPO po);

    /** 用户总数：初始化端点判断"系统是否已存在用户"用。 */
    long count();

    /** 某角色用户数：初始化端点判断"系统是否已存在管理员"用（V6 内置了旅客/商家演示数据后，仍允许创建初始管理员）。 */
    long countByRole(@Param("role") String role);

    /** 用户列表（管理员）：按 username / role / enabled 筛选，任一可选。 */
    List<SysUserPO> search(
            @Param("username") String username,
            @Param("role") String role,
            @Param("enabled") Boolean enabled
    );
}

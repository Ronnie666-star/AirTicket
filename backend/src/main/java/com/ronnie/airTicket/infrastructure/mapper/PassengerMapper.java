package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.PassengerPO;
import com.ronnie.airTicket.infrastructure.persistence.query.PassengerQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 常用乘机人 Mapper。写侧 insert/deleteById 用 PO；读侧 selectByUserId / selectById JOIN sys_user 拿姓名/用户名。
 */
@Mapper
public interface PassengerMapper {

    /** 插入一行，自增主键回填到 po.getId()（useGeneratedKeys）。 */
    int insert(PassengerPO po);

    /** 按主键删除。返回受影响行数（0 = 记录已被并发删除 / 不存在）。 */
    int deleteById(@Param("id") Long id);

    /** 某用户添加的常用乘机人列表（JOIN sys_user 拿姓名/用户名）。 */
    List<PassengerQO> selectByUserId(@Param("userId") Long userId);

    /** 按主键查（JOIN sys_user），用于删除前归属校验。 */
    PassengerQO selectById(@Param("id") Long id);

    /** 是否已添加过同一乘机人（重复添加预判）。 */
    int countByUserIdAndPassengerId(@Param("userId") Long userId, @Param("passengerId") Long passengerId);
}

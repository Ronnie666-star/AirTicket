package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.AirlinePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 航司 Mapper：CRUD + 引用计数（被机型 plane.id_airline 引用）。 */
@Mapper
public interface AirlineMapper {

    List<AirlinePO> list();

    AirlinePO findById(@Param("id") Long id);

    int insert(AirlinePO po);

    int update(AirlinePO po);

    int delete(@Param("id") Long id);

    /** 删除前引用保护：该航司下有多少机型。 */
    long countByAirlineId(@Param("airlineId") Long airlineId);
}

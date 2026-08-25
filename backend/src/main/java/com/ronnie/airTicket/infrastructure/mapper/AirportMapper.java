package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.AirportPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 机场 Mapper：CRUD（可按地区筛选）+ 引用计数（被航班 flight.id_airport_* 引用）。 */
@Mapper
public interface AirportMapper {

    List<AirportPO> list(@Param("region") String region);

    AirportPO findById(@Param("id") Long id);

    int insert(AirportPO po);

    int update(AirportPO po);

    int delete(@Param("id") Long id);

    /** 删除前引用保护：该机场被多少航班引用（出发或到达）。 */
    long countByAirportId(@Param("airportId") Long airportId);
}

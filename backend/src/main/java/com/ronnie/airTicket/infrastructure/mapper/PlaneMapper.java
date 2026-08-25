package com.ronnie.airTicket.infrastructure.mapper;

import com.ronnie.airTicket.infrastructure.persistence.po.PlanePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 机型 Mapper：CRUD + 引用计数（被航班 flight.id_plane 引用）。 */
@Mapper
public interface PlaneMapper {

    List<PlanePO> list();

    PlanePO findById(@Param("id") Long id);

    int insert(PlanePO po);

    int update(PlanePO po);

    int delete(@Param("id") Long id);

    /** 删除前引用保护：该机型被多少航班引用。 */
    long countByPlaneId(@Param("planeId") Long planeId);
}

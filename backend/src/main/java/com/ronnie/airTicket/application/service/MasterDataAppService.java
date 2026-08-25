package com.ronnie.airTicket.application.service;

import com.ronnie.airTicket.domain.exception.DomainException;
import com.ronnie.airTicket.domain.exception.ResourceNotFoundException;
import com.ronnie.airTicket.infrastructure.mapper.AirlineMapper;
import com.ronnie.airTicket.infrastructure.mapper.AirportMapper;
import com.ronnie.airTicket.infrastructure.mapper.ChannelMapper;
import com.ronnie.airTicket.infrastructure.mapper.PlaneMapper;
import com.ronnie.airTicket.infrastructure.persistence.po.AirlinePO;
import com.ronnie.airTicket.infrastructure.persistence.po.AirportPO;
import com.ronnie.airTicket.infrastructure.persistence.po.ChannelPO;
import com.ronnie.airTicket.infrastructure.persistence.po.PlanePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基础数据用例（航司 / 机场 / 机型 / 渠道的增删查改，仅管理员可写）。
 * 校验全放应用层（四张纯引用表无领域行为，不建聚合根）：
 *   唯一性靠表唯一索引 + DuplicateKeyException -> 409 兜底；
 *   plane 着陆重量 <= 起飞重量（应用层预判给友好文案）；
 *   删除前引用计数 countByXxxId > 0 -> 400。
 */
@Service
@RequiredArgsConstructor
public class MasterDataAppService {

    private final AirlineMapper airlineMapper;
    private final AirportMapper airportMapper;
    private final PlaneMapper planeMapper;
    private final ChannelMapper channelMapper;

    // ===== 航司 =====

    public List<AirlineResult> listAirlines() {
        return airlineMapper.list().stream().map(this::toAirlineResult).toList();
    }

    @Transactional
    public AirlineResult createAirline(String name) {
        AirlinePO po = new AirlinePO();
        po.setName(name);
        airlineMapper.insert(po);
        return toAirlineResult(po);
    }

    @Transactional
    public AirlineResult updateAirline(Long id, String name) {
        AirlinePO po = requireAirline(id);
        po.setName(name);
        airlineMapper.update(po);
        return toAirlineResult(po);
    }

    @Transactional
    public void deleteAirline(Long id) {
        requireAirline(id);
        if (airlineMapper.countByAirlineId(id) > 0) {
            throw new DomainException("该航司下有机型引用，不可删除");
        }
        airlineMapper.delete(id);
    }

    // ===== 机场 =====

    public List<AirportResult> listAirports(String region) {
        return airportMapper.list(region).stream().map(this::toAirportResult).toList();
    }

    @Transactional
    public AirportResult createAirport(String name, String region) {
        AirportPO po = new AirportPO();
        po.setName(name);
        po.setRegion(region);
        airportMapper.insert(po);
        return toAirportResult(po);
    }

    @Transactional
    public AirportResult updateAirport(Long id, String name, String region) {
        AirportPO po = requireAirport(id);
        po.setName(name);
        po.setRegion(region);
        airportMapper.update(po);
        return toAirportResult(po);
    }

    @Transactional
    public void deleteAirport(Long id) {
        requireAirport(id);
        if (airportMapper.countByAirportId(id) > 0) {
            throw new DomainException("该机场被航班引用，不可删除");
        }
        airportMapper.delete(id);
    }

    // ===== 机型 =====

    public List<PlaneResult> listPlanes() {
        return planeMapper.list().stream().map(this::toPlaneResult).toList();
    }

    @Transactional
    public PlaneResult createPlane(Long idAirline, String modelName,
                                   BigDecimal length, BigDecimal wingspan, BigDecimal height,
                                   Integer maxTakeoffWeightKg, Integer maxLandingWeightKg,
                                   Integer maxSeatFirstClass, Integer maxSeatBusinessClass,
                                   Integer maxSeatEconomyClass) {
        assertLandingNotAboveTakeoff(maxLandingWeightKg, maxTakeoffWeightKg);
        PlanePO po = new PlanePO();
        po.setIdAirline(idAirline);
        po.setModelName(modelName);
        po.setLength(length);
        po.setWingspan(wingspan);
        po.setHeight(height);
        po.setMaxTakeoffWeightKg(maxTakeoffWeightKg);
        po.setMaxLandingWeightKg(maxLandingWeightKg);
        po.setMaxSeatFirstClass(maxSeatFirstClass);
        po.setMaxSeatBusinessClass(maxSeatBusinessClass);
        po.setMaxSeatEconomyClass(maxSeatEconomyClass);
        planeMapper.insert(po);
        return toPlaneResult(po);
    }

    @Transactional
    public PlaneResult updatePlane(Long id, Long idAirline, String modelName,
                                   BigDecimal length, BigDecimal wingspan, BigDecimal height,
                                   Integer maxTakeoffWeightKg, Integer maxLandingWeightKg,
                                   Integer maxSeatFirstClass, Integer maxSeatBusinessClass,
                                   Integer maxSeatEconomyClass) {
        assertLandingNotAboveTakeoff(maxLandingWeightKg, maxTakeoffWeightKg);
        PlanePO po = requirePlane(id);
        po.setIdAirline(idAirline);
        po.setModelName(modelName);
        po.setLength(length);
        po.setWingspan(wingspan);
        po.setHeight(height);
        po.setMaxTakeoffWeightKg(maxTakeoffWeightKg);
        po.setMaxLandingWeightKg(maxLandingWeightKg);
        po.setMaxSeatFirstClass(maxSeatFirstClass);
        po.setMaxSeatBusinessClass(maxSeatBusinessClass);
        po.setMaxSeatEconomyClass(maxSeatEconomyClass);
        planeMapper.update(po);
        return toPlaneResult(po);
    }

    @Transactional
    public void deletePlane(Long id) {
        requirePlane(id);
        if (planeMapper.countByPlaneId(id) > 0) {
            throw new DomainException("该机型被航班引用，不可删除");
        }
        planeMapper.delete(id);
    }

    // ===== 渠道 =====

    public List<ChannelResult> listChannels() {
        return channelMapper.list().stream().map(this::toChannelResult).toList();
    }

    @Transactional
    public ChannelResult createChannel(String channelName, String apiGatewayUrl) {
        ChannelPO po = new ChannelPO();
        po.setChannelName(channelName);
        po.setApiGatewayUrl(apiGatewayUrl);
        channelMapper.insert(po);
        return toChannelResult(po);
    }

    @Transactional
    public ChannelResult updateChannel(Long id, String channelName, String apiGatewayUrl) {
        ChannelPO po = requireChannel(id);
        po.setChannelName(channelName);
        po.setApiGatewayUrl(apiGatewayUrl);
        channelMapper.update(po);
        return toChannelResult(po);
    }

    @Transactional
    public void deleteChannel(Long id) {
        requireChannel(id);
        if (channelMapper.countByChannelId(id) > 0) {
            throw new DomainException("该渠道被订单引用，不可删除");
        }
        channelMapper.delete(id);
    }

    // ===== 私有方法 =====

    private void assertLandingNotAboveTakeoff(Integer landing, Integer takeoff) {
        if (landing != null && takeoff != null && landing > takeoff) {
            throw new DomainException("着陆重量不得大于起飞重量");
        }
    }

    private AirlinePO requireAirline(Long id) {
        AirlinePO po = airlineMapper.findById(id);
        if (po == null) {
            throw new ResourceNotFoundException("航司不存在");
        }
        return po;
    }

    private AirportPO requireAirport(Long id) {
        AirportPO po = airportMapper.findById(id);
        if (po == null) {
            throw new ResourceNotFoundException("机场不存在");
        }
        return po;
    }

    private PlanePO requirePlane(Long id) {
        PlanePO po = planeMapper.findById(id);
        if (po == null) {
            throw new ResourceNotFoundException("机型不存在");
        }
        return po;
    }

    private ChannelPO requireChannel(Long id) {
        ChannelPO po = channelMapper.findById(id);
        if (po == null) {
            throw new ResourceNotFoundException("渠道不存在");
        }
        return po;
    }

    private AirlineResult toAirlineResult(AirlinePO po) {
        return new AirlineResult(po.getId(), po.getName(), po.getCreateAt());
    }

    private AirportResult toAirportResult(AirportPO po) {
        return new AirportResult(po.getId(), po.getName(), po.getRegion(), po.getCreateAt());
    }

    private PlaneResult toPlaneResult(PlanePO po) {
        return new PlaneResult(
                po.getId(), po.getIdAirline(), po.getModelName(), po.getLength(), po.getWingspan(), po.getHeight(),
                po.getMaxTakeoffWeightKg(), po.getMaxLandingWeightKg(),
                po.getMaxSeatFirstClass(), po.getMaxSeatBusinessClass(), po.getMaxSeatEconomyClass(),
                po.getCreateAt());
    }

    private ChannelResult toChannelResult(ChannelPO po) {
        return new ChannelResult(po.getId(), po.getChannelName(), po.getApiGatewayUrl());
    }
}

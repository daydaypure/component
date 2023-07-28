package com.chenz.component.sign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 签到项配置-入参
 * 主要作来是显示(预留)
 */
@Getter
@Setter
public class GameCommonConfigSignCycleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 周期数量
     */
    private Integer cycleQty;

    /**
     * 签到周期
     */
    private GameCommonConfigSignPrizeDto prizeConfig;

    /**
     * 签到项的其他配置
     */
    private Map<List<Integer>, GameCommonConfigCycleItemDto> cycleItemConfigMap;
}

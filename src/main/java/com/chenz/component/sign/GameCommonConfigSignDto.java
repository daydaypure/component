package com.chenz.component.sign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 签到配置-入参
 */
@Getter
@Setter
public class GameCommonConfigSignDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * relation fix 通过不同的枚举来获取
     * 可以分两种，一种是无固定开始日期的滚动方式: 取当前时间的cycle.size()内,首次签到当作第一天
     * 一种是开始和结束时间是固定日期
     * 决定了从哪一天开始计
     *
     * @see SignCycleTypeEnum
     */
    private Integer cycleType;

    /**
     * 显示的数量
     */
    private Integer displayQty;

    private GameCommonConfigSignCycleDto cycleConfig;
}

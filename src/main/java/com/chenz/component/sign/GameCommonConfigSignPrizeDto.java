package com.chenz.component.sign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 签到奖品配置-入参
 */
@Getter
@Setter
public class GameCommonConfigSignPrizeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * true: 且, false: 或
     */
    private Boolean both;

    private GameCommonConfigSignPrizeDto next;

    /**
     * fixFlag = true 表示每天的奖励都是固定，没有连续签到的加成
     */
    private Boolean fix;
    /**
     * 按通用逻辑，value根据chance获取
     *
     * fixFlag = true，则key = 周期的index
     * fixFlag = false, 则key = 连续签到天数
     */
    private Map<List<Integer>, List<String>> prizeMap;
}
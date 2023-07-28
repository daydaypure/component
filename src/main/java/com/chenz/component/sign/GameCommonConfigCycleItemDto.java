package com.chenz.component.sign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 签到项配置-入参
 * 主要作来是显示(预留)
 */
@Getter
@Setter
public class GameCommonConfigCycleItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dateFormatStr;
}

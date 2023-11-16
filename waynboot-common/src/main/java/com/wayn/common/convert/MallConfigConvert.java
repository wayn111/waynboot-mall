package com.wayn.common.convert;

import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.domain.shop.vo.MallConfigResponseVO;

/**
 * @author: waynaqua
 * @date: 2023/11/13 23:10
 */
public class MallConfigConvert {

    public static MallConfigResponseVO convertMallConfig() {

        return MallConfigResponseVO.builder()
                .freightLimit(WaynConfig.getFreightLimit())
                .freightPrice(WaynConfig.getFreightPrice())
                .mobileUrl(WaynConfig.getMobileUrl())
                .email(WaynConfig.getEmail())
                .name(WaynConfig.getName())
                .unpaidOrderCancelDelayTime(WaynConfig.getUnpaidOrderCancelDelayTime())
                .version(WaynConfig.getVersion())
                .uploadDir(WaynConfig.getUploadDir())
                .build();
    }
}

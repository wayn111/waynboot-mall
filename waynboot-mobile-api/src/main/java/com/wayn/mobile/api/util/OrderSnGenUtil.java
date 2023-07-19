package com.wayn.mobile.api.util;


import com.wayn.common.constant.Constants;
import com.wayn.common.exception.BusinessException;
import com.wayn.data.redis.constant.CacheConstants;
import com.wayn.data.redis.manager.RedisCache;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 订单编号生成帮助类
 */
@Component
@AllArgsConstructor
public class OrderSnGenUtil {

    private RedisCache redisCache;

    /**
     * 返回订单编号，生成规则：秒级时间戳 + 加密用户ID + 今日第几次下单
     *
     * @return 订单编号
     */
    public String generateOrderSn() {
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        Integer orderSnIncrLimit = Constants.ORDER_SN_INCR_LIMIT;
        Long incrKey = redisCache.luaIncrKey(CacheConstants.ORDER_SN_INCR_KEY, orderSnIncrLimit);
        if (incrKey > (long) orderSnIncrLimit) {
            throw new BusinessException("订单编号生成失败");
        }
        return now + String.format("%06d", incrKey);
    }
}

package com.wayn.mobile.api.util;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.util.spring.SpringContextUtil;
import com.wayn.mobile.api.service.IOrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 订单编号生成帮助类
 */
public class OrderSnGenUtil {

    /**
     * 返回订单编号，生成规则：秒级时间戳 + 加密用户ID + 今日第几次下单
     *
     * @param userId 用户ID
     * @return 订单编号
     */
    public static String generateOrderSn(Long userId) {
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        return now + encryptUserId(String.valueOf(userId), 6) + countByOrderSn(userId);
    }

    /**
     * 计算该用户今日内第几次下单
     *
     * @param userId 用户ID
     * @return 该用户今日第几次下单
     */
    public static int countByOrderSn(Long userId) {
        IOrderService orderService = SpringContextUtil.getBean(IOrderService.class);
        return orderService.count(new QueryWrapper<Order>().eq("user_id", userId)
                .gt("create_time", LocalDate.now())
                .lt("create_time", LocalDate.now().plusDays(1)));
    }

    /**
     * 加密用户ID，返回num位字符串
     *
     * @param userId 用户ID
     * @param num    长度
     * @return num位加密字符串
     */
    private static String encryptUserId(String userId, int num) {
        return String.format("%0" + num + "d", Integer.parseInt(userId) + 1);
    }
}

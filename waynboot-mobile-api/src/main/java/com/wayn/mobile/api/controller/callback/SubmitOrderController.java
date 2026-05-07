package com.wayn.mobile.api.controller.callback;

import com.alibaba.fastjson.JSON;
import com.wayn.common.core.service.shop.IMobileOrderService;
import com.wayn.common.model.response.BatchOrderSubmitResVO;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 下单回调接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("callback/order")
public class SubmitOrderController {

    private IMobileOrderService iMobileOrderService;
    private RedisCache redisCache;

    /**
     * 回调下单
     *
     * @param order 订单数据传输对象
     * @return R
     */
    @PostMapping("submit")
    public R submit(String order) {
        OrderDTO orderDTO = JSON.parseObject(order, OrderDTO.class);
        log.info("回调下单开始, orderSn={}, userId={}", orderDTO.getOrderSn(), orderDTO.getUserId());
        try {
            iMobileOrderService.submit(orderDTO);
            redisCache.setCacheObject(RedisKeyEnum.ORDER_RESULT_KEY.getKey(orderDTO.getOrderSn()),
                    "success", RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond());
            log.info("回调下单完成, orderSn={}, result=success", orderDTO.getOrderSn());
            return R.success();
        } catch (Exception e) {
            String errorMsg = "error";
            if (e instanceof BusinessException businessException) {
                errorMsg = businessException.getMsg();
            }
            redisCache.setCacheObject(RedisKeyEnum.ORDER_RESULT_KEY.getKey(orderDTO.getOrderSn()),
                    errorMsg, RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond());
            log.error("回调下单失败, orderSn={}, message={}", orderDTO.getOrderSn(), errorMsg, e);
            return R.error();
        }
    }

    /**
     * 批量回调下单。
     * 该入口服务于 MQ 批量消费，返回每笔订单的落单结果，调用方据此决定整批 ack 或 nack。
     *
     * @param orders 订单 DTO JSON 数组
     * @return 批量下单结果
     */
    @PostMapping("submit/batch")
    public R<BatchOrderSubmitResVO> submitBatch(String orders) {
        List<OrderDTO> orderDTOList = JSON.parseArray(orders, OrderDTO.class);
        if (CollectionUtils.isEmpty(orderDTOList)) {
            log.error("批量回调下单失败, orders 为空");
            return R.error();
        }
        log.info("批量回调下单开始, size={}", orderDTOList.size());
        BatchOrderSubmitResVO result = iMobileOrderService.submitBatch(orderDTOList);
        cacheBatchSubmitResult(result);
        if (!result.getFailedOrderSnMap().isEmpty()) {
            log.error("批量回调下单存在失败订单, successCount={}, failCount={}",
                    result.getSuccessOrderSnList().size(), result.getFailedOrderSnMap().size());
            R<BatchOrderSubmitResVO> response = R.error();
            response.setData(result);
            return response;
        }
        log.info("批量回调下单完成, successCount={}", result.getSuccessOrderSnList().size());
        return R.success(result);
    }

    /**
     * 缓存批量下单每笔订单结果。
     * 前端轮询仍按订单号查询单笔结果，因此批量接口需要回写每个订单的状态。
     *
     * @param result 批量下单结果
     */
    private void cacheBatchSubmitResult(BatchOrderSubmitResVO result) {
        result.getSuccessOrderSnList().forEach(orderSn ->
                redisCache.setCacheObject(RedisKeyEnum.ORDER_RESULT_KEY.getKey(orderSn),
                        "success", RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond()));
        result.getFailedOrderSnMap().forEach((orderSn, errorMsg) ->
                redisCache.setCacheObject(RedisKeyEnum.ORDER_RESULT_KEY.getKey(orderSn),
                        errorMsg, RedisKeyEnum.ORDER_RESULT_KEY.getExpireSecond()));
    }
}

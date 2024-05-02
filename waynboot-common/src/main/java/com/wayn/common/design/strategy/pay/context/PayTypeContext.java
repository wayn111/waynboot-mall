package com.wayn.common.design.strategy.pay.context;

import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付策略配置
 */
@Component
public class PayTypeContext {

    private final Map<Integer, PayTypeInterface> map = new HashMap<>();

    /**
     * 由spring自动注入PayTypeInterface子类
     *
     * @param payTypeInterfaces 支付策略集合
     */
    public PayTypeContext(List<PayTypeInterface> payTypeInterfaces) {
        for (PayTypeInterface item : payTypeInterfaces) {
            map.put(item.getType(), item);
        }
    }

    public PayTypeInterface getInstance(Integer payType) {
        PayTypeInterface payTypeInterface = map.get(payType);
        if (payTypeInterface == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SET_PAY_ERROR);
        }
        return payTypeInterface;
    }
}

package com.wayn.common.design.strategy.refund.context;

import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款策略配置
 */
@Component
public class RefundContext {

    private final Map<Integer, RefundInterface> map = new HashMap<>();

    /**
     * 由spring自动注入RefundInterface子类
     *
     * @param RefundInterface 退款策略集合
     */
    public RefundContext(List<RefundInterface> RefundInterface) {
        for (RefundInterface item : RefundInterface) {
            map.put(item.getType(), item);
        }
    }

    public RefundInterface getInstance(Integer payType) {
        RefundInterface refundInterface = map.get(payType);
        if (refundInterface == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SET_PAY_ERROR);
        }
        return refundInterface;
    }
}

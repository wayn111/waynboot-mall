package com.wayn.common.core.service.shop;

import com.wayn.common.core.entity.shop.OrderStatusLog;
import com.wayn.common.core.mapper.shop.OrderStatusLogMapper;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 订单状态日志服务。
 * 负责把订单状态机流转结果保存为审计日志；日志写入失败只记录错误，不反向打断支付、取消、退款等核心交易链路。
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderStatusLogService {

    private final OrderStatusLogMapper orderStatusLogMapper;

    /**
     * 记录成功状态流转。
     *
     * @param command 状态流转命令
     */
    public void recordSuccess(OrderStatusChangeCommand command) {
        save(command, true, null);
    }

    /**
     * 记录失败状态流转尝试。
     *
     * @param command 状态流转命令
     * @param failReason 失败原因
     */
    public void recordFailure(OrderStatusChangeCommand command, String failReason) {
        save(command, false, failReason);
    }

    /**
     * 保存状态日志。
     * 日志属于排障和审计增强能力，不作为订单主链路成功与否的判定条件，因此这里吞掉日志异常并输出错误上下文。
     *
     * @param command 状态流转命令
     * @param success 是否成功
     * @param failReason 失败原因
     */
    private void save(OrderStatusChangeCommand command, boolean success, String failReason) {
        if (command == null) {
            log.warn("跳过订单状态日志记录, command为空, success={}", success);
            return;
        }
        try {
            orderStatusLogMapper.insert(buildLog(command, success, failReason));
        } catch (Exception e) {
            log.error("记录订单状态日志失败, orderSn={}, changeType={}, success={}",
                    command.orderSn(), command.changeType(), success, e);
        }
    }

    /**
     * 构建订单状态日志实体。
     *
     * @param command 状态流转命令
     * @param success 是否成功
     * @param failReason 失败原因
     * @return 订单状态日志实体
     */
    private OrderStatusLog buildLog(OrderStatusChangeCommand command, boolean success, String failReason) {
        Date now = new Date();
        OrderStatusLog orderStatusLog = new OrderStatusLog();
        orderStatusLog.setOrderId(command.orderId());
        orderStatusLog.setOrderSn(command.orderSn());
        orderStatusLog.setSourceStatus(resolveStatus(command.sourceStatus()));
        orderStatusLog.setTargetStatus(resolveStatus(command.targetStatus()));
        orderStatusLog.setChangeType(resolveChangeType(command));
        orderStatusLog.setOperatorType(command.operatorType());
        orderStatusLog.setOperatorId(command.operatorId());
        orderStatusLog.setSuccess(success);
        orderStatusLog.setFailReason(normalizeText(failReason));
        orderStatusLog.setRemark(normalizeText(command.remark()));
        orderStatusLog.setCreateTime(now);
        orderStatusLog.setUpdateTime(now);
        return orderStatusLog;
    }

    /**
     * 归一化审计日志文本。
     * 失败原因和备注常来自外部回调、人工操作或异常信息，空白字符串没有排障价值，统一收敛为空串便于检索。
     *
     * @param text 原始文本
     * @return 去除首尾空白后的文本；空白输入返回空串
     */
    private String normalizeText(String text) {
        return StringUtils.isBlank(text) ? "" : text.trim();
    }

    /**
     * 解析订单状态枚举为数据库状态值。
     *
     * @param statusEnum 订单状态枚举
     * @return 数据库状态值
     */
    private Short resolveStatus(OrderStatusEnum statusEnum) {
        return statusEnum == null ? null : statusEnum.getStatus();
    }

    /**
     * 解析订单状态变更类型。
     * 状态日志是审计增强能力，字段解析保持空安全，避免调用方漏传变更类型时影响订单主链路。
     *
     * @param command 状态流转命令
     * @return 状态变更类型编码
     */
    private String resolveChangeType(OrderStatusChangeCommand command) {
        return command.changeType() == null ? null : command.changeType().getCode();
    }
}

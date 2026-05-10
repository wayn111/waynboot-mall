package com.wayn.common.core.service.shop;

import com.wayn.common.core.entity.shop.InventoryFlow;
import com.wayn.common.core.mapper.shop.InventoryFlowMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 库存流水服务。
 * 统一创建库存流水并把唯一键冲突转成幂等结果，订单库存编排层据此决定是否继续执行库存条件更新。
 */
@Slf4j
@Service
@AllArgsConstructor
public class InventoryFlowService {

    private final InventoryFlowMapper inventoryFlowMapper;

    /**
     * 保存库存流水。
     * 调用方应在库存变更同一个事务中调用；重复 flowKey 表示该库存副作用已经处理过，返回 false 让调用方跳过重复变更。
     *
     * @param command 库存流水创建命令
     * @return true=本次新写入流水；false=流水已存在
     */
    public boolean saveFlow(InventoryFlowCreateCommand command) {
        InventoryFlow flow = buildFlow(command);
        try {
            inventoryFlowMapper.insert(flow);
            return true;
        } catch (DuplicateKeyException e) {
            // 唯一键冲突是本地消息重试或重复补偿的正常幂等路径，不应升级为业务异常。
            log.info("库存流水已存在, flowKey={}", command.flowKey());
            return false;
        }
    }

    /**
     * 构建库存流水实体。
     *
     * @param command 库存流水创建命令
     * @return 库存流水实体
     */
    private InventoryFlow buildFlow(InventoryFlowCreateCommand command) {
        Date now = new Date();
        InventoryFlow flow = new InventoryFlow();
        flow.setFlowKey(command.flowKey());
        flow.setBizType(command.bizType());
        flow.setBizId(command.bizId());
        flow.setGoodsId(command.goodsId());
        flow.setProductId(command.productId());
        flow.setChangeType(command.changeType());
        flow.setChangeNumber(command.changeNumber());
        flow.setRemark(command.remark());
        flow.setCreateTime(now);
        flow.setUpdateTime(now);
        return flow;
    }
}

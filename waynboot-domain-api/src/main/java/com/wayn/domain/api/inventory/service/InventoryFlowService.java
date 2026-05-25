package com.wayn.domain.api.inventory.service;

/**
 * 库存流水服务接口。
 * 契约层只暴露库存流水幂等写入能力，具体持久化实现由库存领域模块提供。
 */
public interface InventoryFlowService {

    /**
     * 保存库存流水。
     *
     * @param command 库存流水创建命令
     * @return true=本次新写入流水；false=流水已存在或命令非法
     */
    boolean saveFlow(InventoryFlowCreateCommand command);
}

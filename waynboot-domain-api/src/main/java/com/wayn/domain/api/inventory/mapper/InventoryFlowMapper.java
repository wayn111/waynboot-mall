package com.wayn.domain.api.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.inventory.entity.InventoryFlow;

/**
 * 库存流水 Mapper。
 * 只负责库存流水基础持久化，幂等语义由数据库唯一键和 InventoryFlowService 统一处理。
 */
public interface InventoryFlowMapper extends BaseMapper<InventoryFlow> {
}

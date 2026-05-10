package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.shop.InventoryFlow;

/**
 * 库存流水 Mapper。
 * 只负责库存流水基础持久化，幂等语义由数据库唯一键和 InventoryFlowService 统一处理。
 */
public interface InventoryFlowMapper extends BaseMapper<InventoryFlow> {
}

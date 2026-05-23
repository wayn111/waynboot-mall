package com.wayn.common.core.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.message.LocalMessageCompensationLog;

/**
 * 本地消息补偿日志 Mapper。
 * 仅提供基础写入和查询能力，复杂补偿编排统一放在服务层。
 */
public interface LocalMessageCompensationLogMapper extends BaseMapper<LocalMessageCompensationLog> {
}

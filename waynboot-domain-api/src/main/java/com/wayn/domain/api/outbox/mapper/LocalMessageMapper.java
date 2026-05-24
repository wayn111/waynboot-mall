package com.wayn.domain.api.outbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.outbox.entity.LocalMessage;

/**
 * 本地消息 Mapper。
 * 仅提供本地消息表基础 CRUD，复杂状态流转统一收敛在 LocalMessageService。
 */
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {
}

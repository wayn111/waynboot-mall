package com.wayn.common.core.service.tool.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.mapper.tool.QiniuConfigMapper;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 七牛云配置 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-11-13
 */
@Service
public class QiniuConfigServiceImpl extends ServiceImpl<QiniuConfigMapper, QiniuConfig> implements IQiniuConfigService {

}

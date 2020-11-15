package com.wayn.common.core.service.tool.impl;

import com.wayn.common.core.domain.tool.QiniuContent;
import com.wayn.common.core.mapper.tool.QiniuContentMapper;
import com.wayn.common.core.service.tool.IQiniuContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 七牛云文件存储 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
@Service
public class QiniuContentServiceImpl extends ServiceImpl<QiniuContentMapper, QiniuContent> implements IQiniuContentService {

}

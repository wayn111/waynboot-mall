package com.wayn.common.core.service.tool.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.tool.QiniuContent;
import com.wayn.common.core.mapper.tool.QiniuContentMapper;
import com.wayn.common.core.service.tool.IQiniuContentService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private QiniuContentMapper qiniuContentMapper;

    @Override
    public IPage<QiniuContent> listPage(Page<QiniuContent> page, QiniuContent qiniuContent) {
        return qiniuContentMapper.selectQiniuContentListPage(page, qiniuContent);
    }
}

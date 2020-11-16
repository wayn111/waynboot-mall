package com.wayn.common.core.service.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.tool.QiniuContent;

/**
 * <p>
 * 七牛云文件存储 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
public interface IQiniuContentService extends IService<QiniuContent> {

    /**
     * 查询七牛云文件存储分页列表
     *
     * @param page   分页对象
     * @param qiniuContent 查询参数
     * @return qiniuContent分页列表
     */
    IPage<QiniuContent> listPage(Page<QiniuContent> page, QiniuContent qiniuContent);
}

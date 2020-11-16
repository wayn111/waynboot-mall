package com.wayn.common.core.mapper.tool;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.tool.QiniuContent;

/**
 * <p>
 * 七牛云文件存储 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
public interface QiniuContentMapper extends BaseMapper<QiniuContent> {

    IPage<QiniuContent> selectQiniuContentListPage(Page<QiniuContent> page, QiniuContent qiniuContent);
}

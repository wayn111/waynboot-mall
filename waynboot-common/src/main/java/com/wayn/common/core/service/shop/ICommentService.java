package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Comment;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-10-03
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 查询用户评论分页列表
     *
     * @param page    分页对象
     * @param comment 查询参数
     * @return 地址分页列表
     */
    IPage<Comment> listPage(Page<Comment> page, Comment comment);
}

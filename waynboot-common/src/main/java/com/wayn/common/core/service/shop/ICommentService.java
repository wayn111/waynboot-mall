package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Comment;
import com.wayn.common.core.domain.vo.CommentTagNumVO;
import com.wayn.common.core.domain.vo.CommentVO;

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

    /**
     * 根据标签类型查询分页列表
     *
     * @param page    分页对象
     * @param goodsId 商品ID
     * @param tagType 标签类型
     * @return 分页对象
     */
    IPage<CommentVO> selectByTagType(Page<Comment> page, Long goodsId, Integer tagType);

    /**
     * 查询标签数量
     *
     * @param goodsId 商品ID
     * @return 标签数量
     */
    CommentTagNumVO selectTagNum(Long goodsId);

    /**
     * 添加评论信息
     *
     * @param commentVO 评论VO
     * @return boolean
     */
    boolean saveComment(CommentVO commentVO);

}

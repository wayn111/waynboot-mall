package com.wayn.domain.api.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.goods.entity.Comment;
import com.wayn.domain.api.goods.response.CommentTagNumVO;
import com.wayn.domain.api.goods.response.CommentVO;

/**
 * 评论表 Mapper 接口
 *
 * @author wayn
 * @since 2020-10-03
 */
public interface CommentMapper extends BaseMapper<Comment> {

    IPage<Comment> selectListPage(Page<Comment> page, Comment comment);

    IPage<CommentVO> selectByTagType(Page<Comment> page, Long goodsId, Integer tagType);

    CommentTagNumVO selectTagNum(Long goodsId);
}

package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Comment;
import com.wayn.common.core.vo.CommentTagNumVO;
import com.wayn.common.core.vo.CommentVO;
import com.wayn.common.core.mapper.shop.CommentMapper;
import com.wayn.common.core.service.shop.ICommentService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 评论表 服务实现类
 *
 * @author wayn
 * @since 2020-10-03
 */
@Service
@AllArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private CommentMapper commentMapper;

    private IOrderGoodsService iOrderGoodsService;

    @Override
    public IPage<Comment> listPage(Page<Comment> page, Comment comment) {
        return commentMapper.selectListPage(page, comment);
    }

    @Override
    public IPage<CommentVO> selectByTagType(Page<Comment> page, Long goodsId, Integer tagType) {
        return commentMapper.selectByTagType(page, goodsId, tagType);
    }

    @Override
    public CommentTagNumVO selectTagNum(Long goodsId) {
        return commentMapper.selectTagNum(goodsId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveComment(CommentVO commentVO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentVO, comment);
        comment.setCreateTime(new Date());
        comment.setHasPicture(comment.getPicUrls().length > 0);
        if (!save(comment)) {
            throw new BusinessException("保存评论信息失败");
        }
        return iOrderGoodsService.update().set("comment", comment.getId()).eq("id", commentVO.getOrderGoodsId()).update();
    }
}

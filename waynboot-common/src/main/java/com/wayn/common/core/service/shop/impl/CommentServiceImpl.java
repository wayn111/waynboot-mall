package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Comment;
import com.wayn.common.core.domain.vo.CommentTagNumVO;
import com.wayn.common.core.domain.vo.CommentVO;
import com.wayn.common.core.mapper.shop.CommentMapper;
import com.wayn.common.core.service.shop.ICommentService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-10-03
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
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

package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Comment;
import com.wayn.common.core.domain.vo.CommentTagNumVO;
import com.wayn.common.core.domain.vo.CommentVO;
import com.wayn.common.core.service.shop.ICommentService;
import com.wayn.common.util.R;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("comment")
public class CommentController extends BaseController {

    @Autowired
    private ICommentService iCommentService;

    @PostMapping("list")
    public R list(Integer tagType, Long goodsId) {
        Page<Comment> page = getPage();
        IPage<CommentVO> commentIPage = iCommentService.selectByTagType(page, goodsId, tagType);
        return R.success().add("page", commentIPage);
    }

    @PostMapping
    public R addComment(@Valid @RequestBody CommentVO commentVO) {
        commentVO.setUserId(MobileSecurityUtils.getUserId());
        return R.success().add("data", iCommentService.saveComment(commentVO));
    }

    @PostMapping("tagNum")
    public R tagNum(Long goodsId) {
        CommentTagNumVO commentTagNumVO = iCommentService.selectTagNum(goodsId);
        return R.success().add("commentTagNum", commentTagNumVO);
    }
}

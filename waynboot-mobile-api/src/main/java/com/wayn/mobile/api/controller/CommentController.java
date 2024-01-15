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
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户评论接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@RestController
@AllArgsConstructor
@RequestMapping("comment")
public class CommentController extends BaseController {

    private ICommentService iCommentService;

    /**
     * 商品评论列表
     *
     * @param tagType 评论标签类型, 1代表好评 2代表中评 3代表差评
     * @param goodsId 商品id
     * @return R
     */
    @PostMapping("list")
    public R list(Integer tagType, Long goodsId) {
        Page<Comment> page = getPage();
        IPage<CommentVO> commentIPage = iCommentService.selectByTagType(page, goodsId, tagType);
        return R.success().add("page", commentIPage);
    }

    /**
     * 添加评论
     *
     * @param commentVO 评论参数
     * @return R
     */
    @PostMapping
    public R addComment(@Valid @RequestBody CommentVO commentVO) {
        commentVO.setUserId(MobileSecurityUtils.getUserId());
        return R.success().add("data", iCommentService.saveComment(commentVO));
    }

    /**
     * 统计好评、中评、差评数量
     *
     * @param goodsId 商品id
     * @return R
     */
    @PostMapping("tagNum")
    public R tagNum(Long goodsId) {
        CommentTagNumVO commentTagNumVO = iCommentService.selectTagNum(goodsId);
        return R.success().add("commentTagNum", commentTagNumVO);
    }
}

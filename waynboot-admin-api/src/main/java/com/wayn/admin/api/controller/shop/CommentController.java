package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Comment;
import com.wayn.common.core.service.shop.ICommentService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 评论表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-10-03
 */
@RestController
@RequestMapping("shop/comment")
public class CommentController extends BaseController {

    @Autowired
    private ICommentService iCommentService;

    @GetMapping("list")
    public R list(Comment comment) {
        Page<Comment> page = getPage();
        return R.success().add("page", iCommentService.listPage(page, comment));
    }

    @GetMapping("{commentId}")
    public R getComment(@PathVariable Long commentId) {
        return R.success().add("data", iCommentService.getById(commentId));
    }

    @PutMapping
    public R updateComment(@Valid @RequestBody Comment comment) {
        return R.success().add("data", iCommentService.updateById(comment));
    }

    @DeleteMapping("{commentIds}")
    public R delComment(@PathVariable List<Long> commentIds) {
        return R.success().add("data", iCommentService.removeByIds(commentIds));
    }

}

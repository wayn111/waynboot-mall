package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Comment;
import com.wayn.common.core.service.shop.ICommentService;
import com.wayn.common.util.R;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/comment")
public class CommentController extends BaseController {

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

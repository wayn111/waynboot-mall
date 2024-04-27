package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Comment;
import com.wayn.common.core.service.shop.ICommentService;
import com.wayn.util.util.R;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("@ss.hasPermi('shop:comment:list')")
    @GetMapping("list")
    public R list(Comment comment) {
        Page<Comment> page = getPage();
        return R.success(iCommentService.listPage(page, comment));
    }

    @PreAuthorize("@ss.hasPermi('shop:comment:info')")
    @GetMapping("{commentId}")
    public R getComment(@PathVariable Long commentId) {
        return R.success(iCommentService.getById(commentId));
    }

    @PreAuthorize("@ss.hasPermi('shop:comment:update')")
    @PutMapping
    public R updateComment(@Valid @RequestBody Comment comment) {
        return R.success(iCommentService.updateById(comment));
    }

    @PreAuthorize("@ss.hasPermi('shop:comment:delete')")
    @DeleteMapping("{commentIds}")
    public R delComment(@PathVariable List<Long> commentIds) {
        return R.success(iCommentService.removeByIds(commentIds));
    }

}

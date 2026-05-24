package com.wayn.mobile.api.controller.goods;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.goods.entity.Comment;
import com.wayn.domain.api.goods.service.ICommentService;
import com.wayn.domain.api.goods.response.CommentTagNumVO;
import com.wayn.domain.api.goods.response.CommentVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    public R<IPage<CommentVO>> list(Integer tagType, Long goodsId) {
        Page<Comment> page = getPage();
        log.info("查询商品评论开始, goodsId={}, tagType={}, pageNum={}, pageSize={}",
                goodsId, tagType, page.getCurrent(), page.getSize());
        IPage<CommentVO> commentIPage = iCommentService.selectByTagType(page, goodsId, tagType);
        log.info("查询商品评论完成, goodsId={}, count={}", goodsId, commentIPage.getRecords().size());
        return R.success(commentIPage);
    }

    /**
     * 添加评论
     *
     * @param commentVO 评论参数
     * @return R
     */
    @PostMapping
    public R<Boolean> addComment(@Valid @RequestBody CommentVO commentVO) {
        commentVO.setUserId(MobileSecurityUtils.getUserId());
        log.info("新增商品评论开始, userId={}, goodsId={}", commentVO.getUserId(), commentVO.getValueId());
        Boolean saved = iCommentService.saveComment(commentVO);
        log.info("新增商品评论完成, userId={}, goodsId={}, result={}", commentVO.getUserId(), commentVO.getValueId(), saved);
        return R.success(saved);
    }

    /**
     * 统计好评、中评、差评数量
     *
     * @param goodsId 商品id
     * @return R
     */
    @PostMapping("tagNum")
    public R<CommentTagNumVO> tagNum(Long goodsId) {
        log.info("查询评论标签统计开始, goodsId={}", goodsId);
        CommentTagNumVO commentTagNumVO = iCommentService.selectTagNum(goodsId);
        log.info("查询评论标签统计完成, goodsId={}", goodsId);
        return R.success(commentTagNumVO);
    }
}

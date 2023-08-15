package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Keyword;
import com.wayn.common.core.service.shop.IKeywordService;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 关键字管理
 *
 * @author wayn
 * @since 2020-10-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/keyword")
public class KeywordController extends BaseController {

    private IKeywordService iKeywordService;

    @PreAuthorize("@ss.hasPermi('shop:keyword:list')")
    @GetMapping("list")
    public R list(Keyword keyword) {
        Page<Keyword> page = getPage();
        return R.success().add("page", iKeywordService.listPage(page, keyword));
    }

    @PreAuthorize("@ss.hasPermi('shop:keyword:add')")
    @PostMapping
    public R addKeyword(@Validated @RequestBody Keyword keyword) {
        keyword.setCreateTime(new Date());
        return R.result(iKeywordService.save(keyword));
    }

    @PreAuthorize("@ss.hasPermi('shop:keyword:update')")
    @PutMapping
    public R updateKeyword(@Validated @RequestBody Keyword keyword) {
        keyword.setUpdateTime(new Date());
        return R.result(iKeywordService.updateById(keyword));
    }

    @PreAuthorize("@ss.hasPermi('shop:keyword:info')")
    @GetMapping("{keywordId}")
    public R getKeyword(@PathVariable Long keywordId) {
        return R.success().add("data", iKeywordService.getById(keywordId));
    }

    @PreAuthorize("@ss.hasPermi('shop:keyword:delete')")
    @DeleteMapping("{keywordIds}")
    public R deleteKeyword(@PathVariable List<Long> keywordIds) {
        return R.result(iKeywordService.removeByIds(keywordIds));
    }
}

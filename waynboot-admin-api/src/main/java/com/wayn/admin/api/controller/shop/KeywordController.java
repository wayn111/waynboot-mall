package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Keyword;
import com.wayn.common.core.service.shop.IKeywordService;
import com.wayn.util.util.R;
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

    /**
     * 关键字列表
     *
     * @param keyword
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:keyword:list')")
    @GetMapping("list")
    public R<IPage<Keyword>> list(Keyword keyword) {
        Page<Keyword> page = getPage();
        return R.success(iKeywordService.listPage(page, keyword));
    }

    /**
     * 添加关键字
     *
     * @param keyword
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:keyword:add')")
    @PostMapping
    public R<Boolean> addKeyword(@Validated @RequestBody Keyword keyword) {
        keyword.setCreateTime(new Date());
        return R.result(iKeywordService.save(keyword));
    }

    /**
     * 修改关键字
     *
     * @param keyword
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:keyword:update')")
    @PutMapping
    public R<Boolean> updateKeyword(@Validated @RequestBody Keyword keyword) {
        keyword.setUpdateTime(new Date());
        return R.result(iKeywordService.updateById(keyword));
    }


    /**
     * 获取关键字信息
     *
     * @param keywordId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:keyword:info')")
    @GetMapping("{keywordId}")
    public R<Keyword> getKeyword(@PathVariable Long keywordId) {
        return R.success(iKeywordService.getById(keywordId));
    }


    /**
     * 删除关键字
     *
     * @param keywordIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:keyword:delete')")
    @DeleteMapping("{keywordIds}")
    public R<Boolean> deleteKeyword(@PathVariable List<Long> keywordIds) {
        return R.result(iKeywordService.removeByIds(keywordIds));
    }
}

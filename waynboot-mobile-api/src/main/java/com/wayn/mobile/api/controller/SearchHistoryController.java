package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.SearchHistory;
import com.wayn.common.core.service.shop.ISearchHistoryService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索历史接口
 *
 * @author wayn
 * @since 2020-09-23
 */
@RestController
@AllArgsConstructor
@RequestMapping("searchHistory")
public class SearchHistoryController extends BaseController {

    private ISearchHistoryService iSearchHistoryService;

    /**
     * 用户搜索历史列表
     *
     * @return
     */
    @GetMapping("list")
    public R<List<SearchHistory>> list() {
        return R.success(iSearchHistoryService.selectList(MobileSecurityUtils.getUserId()));
    }

    /**
     * 添加搜索历史
     *
     * @param searchHistory 搜索历史参数
     * @return R
     */
    @PostMapping
    public R<Boolean> add(@RequestBody SearchHistory searchHistory) {
        Long memberId = MobileSecurityUtils.getUserId();
        searchHistory.setUserId(memberId);
        return R.result(iSearchHistoryService.save(searchHistory));
    }

    /**
     * 删除搜索历史
     *
     * @param id 搜索历史id
     * @return R
     */
    @DeleteMapping("{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.result(iSearchHistoryService.removeById(id));
    }

    /**
     * 删除当前用户所有搜索历史
     *
     * @return R
     */
    @DeleteMapping("all")
    public R<Boolean> delete() {
        Long memberId = MobileSecurityUtils.getUserId();
        return R.result(iSearchHistoryService.remove(new QueryWrapper<SearchHistory>().eq("user_id", memberId)));
    }

}

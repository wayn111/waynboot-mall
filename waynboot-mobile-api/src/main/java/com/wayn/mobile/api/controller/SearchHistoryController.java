package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.SearchHistory;
import com.wayn.mobile.api.service.ISearchHistoryService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 搜索历史表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
@RestController
@RequestMapping("searchHistory")
public class SearchHistoryController extends BaseController {

    @Autowired
    private ISearchHistoryService iSearchHistoryService;

    @GetMapping("list")
    public R list() {
        return R.success().add("data", iSearchHistoryService.selectList());
    }

    @PostMapping
    public R add(@RequestBody SearchHistory searchHistory) {
        Long memberId = MobileSecurityUtils.getUserId();
        searchHistory.setUserId(memberId);
        return R.result(iSearchHistoryService.save(searchHistory));
    }

    @DeleteMapping("{id}")
    public R delete(@PathVariable Long id) {
        return R.result(iSearchHistoryService.removeById(id));
    }

    @DeleteMapping("all")
    public R delete() {
        Long memberId = MobileSecurityUtils.getUserId();
        return R.result(iSearchHistoryService.remove(new QueryWrapper<SearchHistory>().eq("user_id", memberId)));
    }

}

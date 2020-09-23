package com.wayn.mobile.api.controller;


import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.ISearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 搜索历史表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
@RestController
@RequestMapping("search")
public class SearchHistoryController extends BaseController {

    @Autowired
    private ISearchHistoryService iSearchHistoryService;

    @GetMapping("list")
    public R list() {
        return R.success().add("data", iSearchHistoryService.selectList());
    }
}

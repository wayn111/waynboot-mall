package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Keyword;
import com.wayn.common.core.service.shop.IKeywordService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 关键字表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-10-03
 */
@RestController
@RequestMapping("shop/keyword")
public class KeywordController extends BaseController {

    @Autowired
    private IKeywordService iKeywordService;

    @GetMapping("list")
    public R list(Keyword keyword) {
        Page<Keyword> page = getPage();
        return R.success().add("page", iKeywordService.listPage(page, keyword));
    }
}

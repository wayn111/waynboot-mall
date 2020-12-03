package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Column;
import com.wayn.common.core.service.shop.IColumnService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("shop/column")
public class ColumnController extends BaseController {

    @Autowired
    private IColumnService iColumnService;

    @GetMapping("/list")
    public R list(Column column) {
        Page<Column> page = getPage();
        return R.success().add("page", iColumnService.listPage(page, column));
    }

    @PostMapping
    public R addBanner(@Validated @RequestBody Column column) {
        column.setCreateTime(new Date());
        return R.result(iColumnService.save(column));
    }

    @PutMapping
    public R updateBanner(@Validated @RequestBody Column column) {
        column.setUpdateTime(new Date());
        return R.result(iColumnService.updateById(column));
    }

    @GetMapping("{columnId}")
    public R getBanner(@PathVariable Long columnId) {
        return R.success().add("data", iColumnService.getById(columnId));
    }

    @DeleteMapping("{columnIds}")
    public R deleteBanner(@PathVariable List<Long> columnIds) {
        return R.result(iColumnService.removeByIds(columnIds));
    }

}

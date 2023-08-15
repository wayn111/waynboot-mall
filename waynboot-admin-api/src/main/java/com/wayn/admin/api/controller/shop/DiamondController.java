package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 金刚区管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/diamond")
public class DiamondController extends BaseController {

    private IDiamondService iDiamondService;

    @PreAuthorize("@ss.hasPermi('shop:diamond:list')")
    @GetMapping("/list")
    public R list(Diamond diamond) {
        Page<Diamond> page = getPage();
        IPage<Diamond> diamondIPage = iDiamondService.listPage(page, diamond);
        return R.success().add("page", diamondIPage);
    }

    @PreAuthorize("@ss.hasPermi('shop:diamond:add')")
    @PostMapping
    public R addDiamond(@Validated @RequestBody Diamond diamond) {
        diamond.setCreateTime(new Date());
        return R.result(iDiamondService.save(diamond));
    }

    @PreAuthorize("@ss.hasPermi('shop:diamond:update')")
    @PutMapping
    public R updateDiamond(@Validated @RequestBody Diamond diamond) {
        diamond.setUpdateTime(new Date());
        return R.result(iDiamondService.updateById(diamond));
    }

    @PreAuthorize("@ss.hasPermi('shop:diamond:info')")
    @GetMapping("{diamondId}")
    public R getDiamond(@PathVariable Long diamondId) {
        return R.success().add("data", iDiamondService.getById(diamondId));
    }

    @PreAuthorize("@ss.hasPermi('shop:diamond:delete')")
    @DeleteMapping("{diamondIds}")
    public R deleteDiamond(@PathVariable List<Long> diamondIds) {
        return R.result(iDiamondService.removeByIds(diamondIds));
    }
}

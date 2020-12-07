package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("shop/diamond")
public class DiamondController extends BaseController {

    @Autowired
    private IDiamondService iDiamondService;

    @GetMapping("/list")
    public R list(Diamond diamond) {
        Page<Diamond> page = getPage();
        IPage<Diamond> diamondIPage = iDiamondService.listPage(page, diamond);
        return R.success().add("page", diamondIPage);
    }

    @PostMapping
    public R addDiamond(@Validated @RequestBody Diamond diamond) {
        diamond.setCreateTime(new Date());
        return R.result(iDiamondService.save(diamond));
    }

    @PutMapping
    public R updateDiamond(@Validated @RequestBody Diamond diamond) {
        diamond.setUpdateTime(new Date());
        return R.result(iDiamondService.updateById(diamond));
    }

    @GetMapping("{diamondId}")
    public R getDiamond(@PathVariable Long diamondId) {
        return R.success().add("data", iDiamondService.getById(diamondId));
    }

    @DeleteMapping("{diamondIds}")
    public R deleteDiamond(@PathVariable List<Long> diamondIds) {
        return R.result(iDiamondService.removeByIds(diamondIds));
    }
}

package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.util.R;
import com.wayn.mobile.design.strategy.context.DiamondJumpContext;
import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("diamond")
public class DiamondController extends BaseController {

    @Autowired
    private IDiamondService iDiamondService;

    @Autowired
    private DiamondJumpContext diamondJumpContext;

    @GetMapping("getGoodsList")
    public R getGoodsList(Long diamondId) {
        Page<Goods> page = getPage();
        Diamond diamond = iDiamondService.getById(diamondId);
        DiamondJumpType diamondJumpType = diamondJumpContext.getInstance(diamond.getJumpType());
        if (diamondJumpType == null) throw new AssertionError();
        List<Goods> goods = diamondJumpType.getGoods(page, diamond);
        return R.success().add("diamond", diamond).add("goods", goods);
    }
}

package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.response.DiamondGoodsResVO;
import com.wayn.util.util.R;
import com.wayn.mobile.design.strategy.context.DiamondJumpContext;
import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 金刚区接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@RestController
@AllArgsConstructor
@RequestMapping("diamond")
public class DiamondController extends BaseController {

    private IDiamondService iDiamondService;

    private DiamondJumpContext diamondJumpContext;

    /**
     * 金刚区跳转
     *
     * @param diamondId 金刚区id
     * @return R
     */
    @GetMapping("getGoodsList")
    public R getGoodsList(Long diamondId) {
        Page<Goods> page = getPage();
        Diamond diamond = iDiamondService.getById(diamondId);
        DiamondJumpType diamondJumpType = diamondJumpContext.getInstance(diamond.getJumpType());
        List<Goods> goods = diamondJumpType.getGoods(page, diamond);
        DiamondGoodsResVO resVO = new DiamondGoodsResVO();
        resVO.setDiamond(diamond);
        resVO.setGoods(goods);
        return R.success(resVO);
    }
}

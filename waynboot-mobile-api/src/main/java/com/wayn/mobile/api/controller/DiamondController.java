package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.ColumnGoodsRelation;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("diamond")
public class DiamondController extends BaseController {

    @Autowired
    private IColumnService iColumnService;

    @Autowired
    private IColumnGoodsRelationService iColumnGoodsRelationService;

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IDiamondService iDiamondService;

    @Autowired
    private IGoodsService iGoodsService;

    @GetMapping("getGoodsList")
    public R getGoodsList(Long diamondId) {
        Page<Goods> page = getPage();
        Diamond diamond = iDiamondService.getById(diamondId);
        if (diamond.getJumpType() == 0) {
            List<ColumnGoodsRelation> goodsRelationList = iColumnGoodsRelationService.list(new QueryWrapper<ColumnGoodsRelation>()
                    .eq("column_id", diamond.getValueId()));
            List<Long> goodsIdList = goodsRelationList.stream().map(ColumnGoodsRelation::getGoodsId).collect(Collectors.toList());
            Page<Goods> goodsPage = iGoodsService.page(page, new QueryWrapper<Goods>().in("id", goodsIdList).eq("is_on_sale", true));
            return R.success().add("diamond", diamond).add("goods", goodsPage.getRecords());
        } else if (diamond.getJumpType() == 1) {
            List<Long> cateList = Arrays.asList(diamond.getValueId());
            R success = iGoodsService.selectListPageByCateIds(page, cateList);
            return success.add("diamond", diamond);
        }
        return R.error();
    }
}

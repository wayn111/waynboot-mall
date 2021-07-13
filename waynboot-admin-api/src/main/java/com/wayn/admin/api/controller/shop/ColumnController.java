package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Column;
import com.wayn.common.core.domain.shop.ColumnGoodsRelation;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.ColumnVO;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import com.wayn.common.core.service.shop.IColumnService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("shop/column")
public class ColumnController extends BaseController {

    @Autowired
    private IColumnService iColumnService;

    @Autowired
    private IColumnGoodsRelationService iColumnGoodsRelationService;

    @Autowired
    private IGoodsService iGoodsService;

    @GetMapping("/list")
    public R list(Column column) {
        Page<Column> page = getPage();
        IPage<Column> columnIPage = iColumnService.listPage(page, column);
        List<ColumnVO> columnVOS = columnIPage.getRecords().stream().map(item -> {
            ColumnVO columnVO = new ColumnVO();
            try {
                BeanUtils.copyProperties(columnVO, item);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
            Integer count = iColumnGoodsRelationService.getGoodsNum(item.getId());
            columnVO.setGoodsNum(count);
            return columnVO;
        }).collect(Collectors.toList());
        return R.success().add("page", formatPage(columnIPage, columnVOS));
    }

    @GetMapping("/listAll")
    public R listAll() {
        List<Column> columnList = iColumnService.list();
        return R.success().add("data", columnList);
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

    @GetMapping("bindGoodsList")
    public R bindGoodsList(Goods goods, Long columnId) {
        Page<Goods> page = getPage();
        List<ColumnGoodsRelation> goodsRelationList = iColumnGoodsRelationService.list(new QueryWrapper<ColumnGoodsRelation>()
                .eq("column_id", columnId));
        if (CollectionUtils.isEmpty(goodsRelationList)) {
            return R.success().add("page", new Page<Goods>());
        }
        List<Long> columnGoodsIds = goodsRelationList.stream().map(ColumnGoodsRelation::getGoodsId).collect(Collectors.toList());
        IPage<Goods> listPage = iGoodsService.listColumnBindGoodsPage(page, goods, columnGoodsIds);
        return R.success().add("page", listPage);
    }

    @GetMapping("unBindGoodsList")
    public R unBindGoodsList(Goods goods, Long columnId) {
        Page<Goods> page = getPage();
        List<ColumnGoodsRelation> goodsRelationList = iColumnGoodsRelationService.list(new QueryWrapper<ColumnGoodsRelation>()
                .eq("column_id", columnId));
        List<Long> columnGoodsIds = goodsRelationList.stream().map(ColumnGoodsRelation::getGoodsId).collect(Collectors.toList());
        IPage<Goods> listPage = iGoodsService.listColumnUnBindGoodsPage(page, goods, columnGoodsIds);
        return R.success().add("page", listPage);
    }

    @PostMapping("goods")
    public R addGoods(@RequestBody ColumnGoodsRelation columnGoodsRelation) {
        return R.result(iColumnGoodsRelationService.save(columnGoodsRelation));
    }

    @DeleteMapping("goods")
    public R deleteGoods(@RequestBody ColumnGoodsRelation columnGoodsRelation) {
        return R.result(iColumnGoodsRelationService.remove(new QueryWrapper<ColumnGoodsRelation>()
                .eq("goods_id", columnGoodsRelation.getGoodsId())
                .eq("column_id", columnGoodsRelation.getColumnId())));
    }
}

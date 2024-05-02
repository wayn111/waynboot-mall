package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Column;
import com.wayn.common.core.entity.shop.ColumnGoodsRelation;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import com.wayn.common.core.service.shop.IColumnService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.response.ColumnManagerResVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商城栏目管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("shop/column")
public class ColumnController extends BaseController {

    private IColumnService iColumnService;

    private IColumnGoodsRelationService iColumnGoodsRelationService;

    private IGoodsService iGoodsService;

    /**
     * 栏目列表
     *
     * @param column
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:column:list')")
    @GetMapping("/list")
    public R<IPage<ColumnManagerResVO>> list(Column column) {
        Page<Column> page = getPage();
        IPage<Column> columnIPage = iColumnService.listPage(page, column);
        List<ColumnManagerResVO> columnManagerResVOS = columnIPage.getRecords().stream().map(item -> {
            ColumnManagerResVO columnManagerResVO = new ColumnManagerResVO();
            BeanUtils.copyProperties(item, columnManagerResVO);
            Integer count = iColumnGoodsRelationService.getGoodsNum(item.getId());
            columnManagerResVO.setGoodsNum(count);
            return columnManagerResVO;
        }).collect(Collectors.toList());
        return R.success(formatPage(columnIPage, columnManagerResVOS));
    }

    /**
     * 获取所有栏目信息
     *
     * @return
     */
    @GetMapping("/listAll")
    public R<List<Column>> listAll() {
        List<Column> columnList = iColumnService.list();
        return R.success(columnList);
    }

    /**
     * 添加栏目
     *
     * @param column
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:column:add')")
    @PostMapping
    public R<Boolean> addColumn(@Validated @RequestBody Column column) {
        column.setCreateTime(new Date());
        return R.result(iColumnService.save(column));
    }

    /**
     * 修改栏目
     *
     * @param column
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:column:update')")
    @PutMapping
    public R<Boolean> updateColumn(@Validated @RequestBody Column column) {
        column.setUpdateTime(new Date());
        return R.result(iColumnService.updateById(column));
    }

    /**
     * 获取栏目信息
     *
     * @param columnId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:column:info')")
    @GetMapping("{columnId}")
    public R<Column> getColumn(@PathVariable Long columnId) {
        return R.success(iColumnService.getById(columnId));
    }

    /**
     * 删除栏目
     *
     * @param columnIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:column:delete')")
    @DeleteMapping("{columnIds}")
    public R<Boolean> deleteColumn(@PathVariable List<Long> columnIds) {
        return R.result(iColumnService.removeByIds(columnIds));
    }

    /**
     * 栏目已经绑定商品列表
     *
     * @param goods
     * @param columnId
     * @return
     */
    @GetMapping("bindGoodsList")
    public R<IPage<Goods>> bindGoodsList(Goods goods, Long columnId) {
        Page<Goods> page = getPage();
        List<ColumnGoodsRelation> goodsRelationList = iColumnGoodsRelationService.list(new QueryWrapper<ColumnGoodsRelation>()
                .eq("column_id", columnId));
        if (CollectionUtils.isEmpty(goodsRelationList)) {
            return R.success(new Page<Goods>());
        }
        List<Long> columnGoodsIds = goodsRelationList.stream().map(ColumnGoodsRelation::getGoodsId).collect(Collectors.toList());
        IPage<Goods> listPage = iGoodsService.listColumnBindGoodsPage(page, goods, columnGoodsIds);
        return R.success(listPage);
    }

    /**
     * 栏目已经解绑商品列表
     *
     * @param goods
     * @param columnId
     * @return
     */
    @GetMapping("unBindGoodsList")
    public R<IPage<Goods>> unBindGoodsList(Goods goods, Long columnId) {
        Page<Goods> page = getPage();
        List<ColumnGoodsRelation> goodsRelationList = iColumnGoodsRelationService.list(new QueryWrapper<ColumnGoodsRelation>()
                .eq("column_id", columnId));
        List<Long> columnGoodsIds = goodsRelationList.stream().map(ColumnGoodsRelation::getGoodsId).collect(Collectors.toList());
        IPage<Goods> listPage = iGoodsService.listColumnUnBindGoodsPage(page, goods, columnGoodsIds);
        return R.success(listPage);
    }

    /**
     * 栏目添加商品
     *
     * @param columnGoodsRelation
     * @return
     */
    @PostMapping("goods")
    public R<Boolean> addGoods(@RequestBody ColumnGoodsRelation columnGoodsRelation) {
        return R.result(iColumnGoodsRelationService.save(columnGoodsRelation));
    }

    /**
     * 栏目删除商品
     *
     * @param columnGoodsRelation
     * @return
     */
    @DeleteMapping("goods")
    public R<Boolean> deleteGoods(@RequestBody ColumnGoodsRelation columnGoodsRelation) {
        return R.result(iColumnGoodsRelationService.remove(new QueryWrapper<ColumnGoodsRelation>()
                .eq("goods_id", columnGoodsRelation.getGoodsId())
                .eq("column_id", columnGoodsRelation.getColumnId())));
    }
}

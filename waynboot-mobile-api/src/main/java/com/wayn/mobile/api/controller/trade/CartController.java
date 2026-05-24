package com.wayn.mobile.api.controller.trade;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.cart.service.ICartService;
import com.wayn.common.model.request.CartAddReqVO;
import com.wayn.common.model.request.CartUpdateReqVO;
import com.wayn.domain.api.cart.response.CartResponseVO;
import com.wayn.domain.api.cart.response.CheckedGoodsResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车接口
 *
 * @author wayn
 * @since 2020-08-03
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("cart")
public class CartController extends BaseController {

    private final ICartService iCartService;

    /**
     * 购物车列表
     *
     * @return
     */
    @GetMapping("list")
    public R<List<CartResponseVO>> list() {
        Long userId = MobileSecurityUtils.getUserId();
        Page<Cart> page = getPage();
        log.info("查询购物车开始, userId={}, pageNum={}, pageSize={}", userId, page.getCurrent(), page.getSize());
        List<CartResponseVO> cartList = iCartService.list(page, userId);
        log.info("查询购物车完成, userId={}, count={}", userId, cartList.size());
        return R.success(cartList);
    }

    /**
     * 添加购物车
     *
     * @return R
     */
    @PostMapping
    public R<Boolean> add(@RequestBody CartAddReqVO reqVO) {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("新增购物车开始, userId={}, goodsId={}, productId={}, number={}",
                userId, reqVO.getGoodsId(), reqVO.getProductId(), reqVO.getNumber());
        iCartService.add(toCart(reqVO), userId);
        log.info("新增购物车完成, userId={}, goodsId={}, productId={}", userId, reqVO.getGoodsId(), reqVO.getProductId());
        return R.success();
    }

    /**
     * 往购物车中添加默认商品
     *
     * @return R
     */
    @PostMapping("addDefaultGoodsProduct")
    public R<Boolean> addDefaultGoodsProduct(@RequestBody CartAddReqVO reqVO) {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("新增默认货品购物车开始, userId={}, goodsId={}, number={}", userId, reqVO.getGoodsId(), reqVO.getNumber());
        iCartService.addDefaultGoodsProduct(toCart(reqVO), userId);
        log.info("新增默认货品购物车完成, userId={}, goodsId={}", userId, reqVO.getGoodsId());
        return R.success();
    }

    /**
     * 更新购物车
     *
     * @param cart 更新参数
     * @return R
     */
    @PutMapping
    public R<Boolean> update(@RequestBody CartUpdateReqVO reqVO) {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("更新购物车勾选状态开始, userId={}, cartId={}, checked={}", userId, reqVO.getId(), reqVO.getChecked());
        Boolean updated = iCartService.updateChecked(reqVO.getId(), reqVO.getChecked(), userId);
        log.info("更新购物车勾选状态完成, userId={}, cartId={}, result={}", userId, reqVO.getId(), updated);
        return R.result(updated);
    }

    /**
     * 修改购物车商品数量
     *
     * @param cartId 购物车id
     * @param number 更新数量
     * @return R
     */
    @PostMapping("changeNum/{cartId}/{number}")
    public R<Boolean> changeNum(@PathVariable Long cartId, @PathVariable Integer number) {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("更新购物车数量开始, userId={}, cartId={}, number={}", userId, cartId, number);
        Boolean updated = iCartService.changeNum(cartId, number);
        log.info("更新购物车数量完成, userId={}, cartId={}, result={}", userId, cartId, updated);
        return R.result(updated);
    }

    /**
     * 删除购物车商品
     *
     * @param cartId 购物车id
     * @return R
     */
    @DeleteMapping("{cartId}")
    public R<Boolean> delete(@PathVariable Long cartId) {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("删除购物车开始, userId={}, cartId={}", userId, cartId);
        Boolean removed = iCartService.remove(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getId, cartId)
                .eq(Cart::getUserId, userId));
        log.info("删除购物车完成, userId={}, cartId={}, result={}", userId, cartId, removed);
        return R.result(removed);
    }

    /**
     * 统计购物车中现有商品数量
     *
     * @return R
     */
    @GetMapping("goodsCount")
    public R<Long> goodsCount() {
        Long userId = MobileSecurityUtils.getUserId();
        Long count = iCartService.goodsCount(userId);
        log.info("查询购物车商品数量完成, userId={}, count={}", userId, count);
        return R.success(count);
    }

    /**
     * 统计购物车中已勾选商品数量
     *
     * @return R
     */
    @PostMapping("getCheckedGoods")
    public R<CheckedGoodsResVO> getCheckedGoods() {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("查询已勾选购物车汇总开始, userId={}", userId);
        CheckedGoodsResVO resVO = iCartService.getCheckedGoods(userId);
        log.info("查询已勾选购物车汇总完成, userId={}, count={}, couponCount={}",
                userId,
                resVO.getData() == null ? 0 : resVO.getData().size(),
                resVO.getCouponList() == null ? 0 : resVO.getCouponList().size());
        return R.success(resVO);
    }

    /**
     * 把购物车请求映射为实体。
     *
     * @param reqVO 购物车请求
     * @return 购物车实体
     */
    private Cart toCart(CartAddReqVO reqVO) {
        return BeanUtil.copyProperties(reqVO, Cart.class);
    }
}

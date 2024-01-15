package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IMobileOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 订单接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("order")
public class OrderController extends BaseController {

    private IMobileOrderService iMobileOrderService;

    /**
     * 根据订单编号获取订单详情
     *
     * @param orderSn 订单编号
     * @return R
     */
    @GetMapping("detail/{orderSn}")
    public R detail(@PathVariable String orderSn) {
        return iMobileOrderService.getOrderDetailByOrderSn(orderSn);
    }

    /**
     * 根据订单转状态展示用户订单列表
     *
     * @param showType 展示类型 0全部 1待付款订单 2待发货订单 3待收货订单 4待评价订单
     * @return
     */
    @GetMapping("list")
    public R list(@RequestParam(defaultValue = "0") Integer showType) {
        Page<Order> page = getPage();
        return iMobileOrderService.selectListPage(page, showType);
    }

    /**
     * 订单状态统计
     *
     * @return R
     */
    @PostMapping("statusCount")
    public R statusCount() {
        return iMobileOrderService.statusCount();
    }

    /**
     * 下单接口
     *
     * @param orderVO 订单参数
     * @return R
     */
    @PostMapping("submit")
    public R submit(@RequestBody OrderVO orderVO) throws Exception {
        return iMobileOrderService.asyncSubmit(orderVO);
    }

    /**
     * 下单结果查询
     *
     * @param orderSn 订单编号
     * @return R
     */
    @GetMapping("searchResult/{orderSn}")
    public R searchResult(@PathVariable String orderSn) {
        return iMobileOrderService.searchResult(orderSn);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("cancel/{orderId}")
    public R cancel(@PathVariable Long orderId) {
        return iMobileOrderService.cancel(orderId);
    }

    /**
     * 确认提交订单
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("confirm/{orderId}")
    public R confirm(@PathVariable Long orderId) {
        return iMobileOrderService.confirm(orderId);
    }

    /**
     * 删除订单
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("delete/{orderId}")
    public R delete(@PathVariable Long orderId) {
        return iMobileOrderService.delete(orderId);
    }

}

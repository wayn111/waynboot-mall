package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.service.shop.IMobileOrderService;
import com.wayn.common.core.vo.OrderDetailVO;
import com.wayn.common.request.OrderCommitReqVO;
import com.wayn.common.response.OrderListResVO;
import com.wayn.common.response.OrderStatusCountResVO;
import com.wayn.common.response.SubmitOrderResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
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
    public R<OrderDetailVO> detail(@PathVariable String orderSn) {
        return R.success(iMobileOrderService.getOrderDetailByOrderSn(orderSn));
    }

    /**
     * 根据订单转状态展示用户订单列表
     *
     * @param showType 展示类型 0全部 1待付款订单 2待发货订单 3待收货订单 4待评价订单
     * @return
     */
    @GetMapping("list")
    public R<OrderListResVO> list(@RequestParam(defaultValue = "0") Integer showType) {
        Page<Order> page = getPage();
        return R.success(iMobileOrderService.selectListPage(page, showType, MobileSecurityUtils.getUserId()));
    }

    /**
     * 订单状态统计
     *
     * @return R
     */
    @PostMapping("statusCount")
    public R<OrderStatusCountResVO> statusCount() {
        return R.success(iMobileOrderService.statusCount(MobileSecurityUtils.getUserId()));
    }

    /**
     * 下单接口
     *
     * @param orderCommitReqVO 订单参数
     * @return R
     */
    @PostMapping("submit")
    public R<SubmitOrderResVO> submit(@RequestBody OrderCommitReqVO orderCommitReqVO) throws Exception {
        return R.success(iMobileOrderService.asyncSubmit(orderCommitReqVO, MobileSecurityUtils.getUserId()));
    }

    /**
     * 下单结果查询
     *
     * @param orderSn 订单编号
     * @return R
     */
    @GetMapping("searchResult/{orderSn}")
    public R<Boolean> searchResult(@PathVariable String orderSn) {
        String result = iMobileOrderService.searchResult(orderSn);
        if (!"success".equals(result)) {
            return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR.getCode(), result);
        }
        return R.success();
    }

    /**
     * 取消订单
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("cancel/{orderId}")
    public R<Boolean> cancel(@PathVariable Long orderId) {
        iMobileOrderService.cancel(orderId);
        return R.success();
    }

    /**
     * 确认提交订单
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("confirm/{orderId}")
    public R<Boolean> confirm(@PathVariable Long orderId) {
        iMobileOrderService.confirm(orderId);
        return R.success();
    }

    /**
     * 删除订单
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("delete/{orderId}")
    public R<Boolean> delete(@PathVariable Long orderId) {
        iMobileOrderService.delete(orderId);
        return R.success();
    }

}

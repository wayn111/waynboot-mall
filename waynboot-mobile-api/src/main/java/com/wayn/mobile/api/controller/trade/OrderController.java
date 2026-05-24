package com.wayn.mobile.api.controller.trade;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.service.IMobileOrderService;
import com.wayn.domain.api.trade.response.OrderDetailVO;
import com.wayn.domain.api.trade.request.OrderCommitReqVO;
import com.wayn.domain.api.trade.response.OrderListResVO;
import com.wayn.domain.api.trade.response.OrderStatusCountResVO;
import com.wayn.domain.api.trade.response.SubmitOrderResVO;
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
        log.info("查询订单详情开始, userId={}, orderSn={}", MobileSecurityUtils.getUserId(), orderSn);
        OrderDetailVO detailVO = iMobileOrderService.getOrderDetailByOrderSn(orderSn);
        log.info("查询订单详情完成, userId={}, orderSn={}", MobileSecurityUtils.getUserId(), orderSn);
        return R.success(detailVO);
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
        Long userId = MobileSecurityUtils.getUserId();
        log.info("查询订单列表开始, userId={}, showType={}, pageNum={}, pageSize={}", userId, showType, page.getCurrent(), page.getSize());
        OrderListResVO resVO = iMobileOrderService.selectListPage(page, showType, userId);
        log.info("查询订单列表完成, userId={}, showType={}, count={}",
                userId, showType, resVO.getData() == null ? 0 : resVO.getData().size());
        return R.success(resVO);
    }

    /**
     * 订单状态统计
     *
     * @return R
     */
    @PostMapping("statusCount")
    public R<OrderStatusCountResVO> statusCount() {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("查询订单状态统计开始, userId={}", userId);
        OrderStatusCountResVO resVO = iMobileOrderService.statusCount(userId);
        log.info("查询订单状态统计完成, userId={}", userId);
        return R.success(resVO);
    }

    /**
     * 下单接口
     *
     * @param orderCommitReqVO 订单参数
     * @return R
     */
    @PostMapping("submit")
    public R<SubmitOrderResVO> submit(@RequestBody OrderCommitReqVO orderCommitReqVO) throws Exception {
        Long userId = MobileSecurityUtils.getUserId();
        log.info("异步提交订单开始, userId={}, addressId={}, cartCount={}, couponId={}",
                userId,
                orderCommitReqVO.getAddressId(),
                orderCommitReqVO.getCartIdArr() == null ? 0 : orderCommitReqVO.getCartIdArr().size(),
                orderCommitReqVO.getUserCouponId());
        SubmitOrderResVO resVO = iMobileOrderService.asyncSubmit(orderCommitReqVO, userId);
        log.info("异步提交订单完成, userId={}, orderSn={}", userId, resVO.getOrderSn());
        return R.success(resVO);
    }

    /**
     * 下单结果查询
     *
     * @param orderSn 订单编号
     * @return R
     */
    @GetMapping("searchResult/{orderSn}")
    public R<Boolean> searchResult(@PathVariable String orderSn) {
        log.info("查询下单结果开始, userId={}, orderSn={}", MobileSecurityUtils.getUserId(), orderSn);
        String result = iMobileOrderService.searchResult(orderSn);
        if (!"success".equals(result)) {
            log.warn("查询下单结果失败, userId={}, orderSn={}, result={}", MobileSecurityUtils.getUserId(), orderSn, result);
            return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR.getCode(), result);
        }
        log.info("查询下单结果完成, userId={}, orderSn={}", MobileSecurityUtils.getUserId(), orderSn);
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
        log.info("取消订单开始, userId={}, orderId={}", MobileSecurityUtils.getUserId(), orderId);
        iMobileOrderService.cancel(orderId);
        log.info("取消订单完成, userId={}, orderId={}", MobileSecurityUtils.getUserId(), orderId);
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
        log.info("确认收货开始, userId={}, orderId={}", MobileSecurityUtils.getUserId(), orderId);
        iMobileOrderService.confirm(orderId);
        log.info("确认收货完成, userId={}, orderId={}", MobileSecurityUtils.getUserId(), orderId);
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
        log.info("删除订单开始, userId={}, orderId={}", MobileSecurityUtils.getUserId(), orderId);
        iMobileOrderService.delete(orderId);
        log.info("删除订单完成, userId={}, orderId={}", MobileSecurityUtils.getUserId(), orderId);
        return R.success();
    }

}

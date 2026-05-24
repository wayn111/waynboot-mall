package com.wayn.admin.api.controller.shop;

import cn.hutool.core.bean.BeanUtil;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.admin.framework.config.properties.ExpressProperties;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.service.IOrderService;
import com.wayn.common.model.dto.OrderExportDTO;
import com.wayn.domain.api.trade.request.OrderManagerReqVO;
import com.wayn.domain.api.trade.request.OrderRefundReqVO;
import com.wayn.domain.api.trade.request.ShipRequestVO;
import com.wayn.common.model.response.ExpressVendorResVO;
import com.wayn.domain.api.trade.response.OrderDetailResVO;
import com.wayn.domain.api.trade.response.OrderManagerResVO;
import com.wayn.util.util.R;
import com.wayn.util.util.excel.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * 订单管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("shop/order")
public class OrderController extends BaseController {

    private ExpressProperties expressProperties;

    private IOrderService iOrderService;

    /**
     * 订单列表
     *
     * @param order
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:order:list')")
    @GetMapping("list")
    public R<IPage<OrderManagerResVO>> list(OrderManagerReqVO order) {
        Page<Order> page = getPage();
        IPage<OrderManagerResVO> orderPage = iOrderService.listPage(page, order);
        return R.success(orderPage);
    }

    /**
     * 订单详情
     *
     * @param orderId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:order:info')")
    @GetMapping("{orderId}")
    public R<OrderDetailResVO> info(@PathVariable Long orderId) {
        OrderDetailResVO resVO = iOrderService.detail(orderId);
        return R.success(resVO);
    }

    /**
     * 删除订单
     *
     * @param orderId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:order:delete')")
    @DeleteMapping("{orderId}")
    public R<Boolean> deleteOrder(@PathVariable Long orderId) {
        Boolean removed = iOrderService.removeById(orderId);
        log.info("删除订单完成, orderId={}, result={}", orderId, removed);
        return R.result(removed);
    }

    /**
     * 确认退款
     *
     * @param reqVO
     * @return
     * @throws UnsupportedEncodingException
     * @throws WxPayException
     * @throws AlipayApiException
     */
    @PreAuthorize("@ss.hasPermi('shop:order:refund')")
    @PostMapping("refund")
    public R<Boolean> refund(@RequestBody @Validated OrderRefundReqVO reqVO) throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        iOrderService.refund(reqVO);
        log.info("订单退款完成, orderSn={}, refundMoney={}", reqVO.getOrderSn(), reqVO.getRefundMoney());
        return R.success();
    }

    /**
     * 发货渠道列表
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:order:ship')")
    @PostMapping("listChannel")
    public R<List<ExpressVendorResVO>> channel() {
        List<ExpressVendorResVO> vendors = expressProperties.getVendors().stream()
                .map(item -> {
                    ExpressVendorResVO resVO = new ExpressVendorResVO();
                    resVO.setCode(item.get("code"));
                    resVO.setName(item.get("name"));
                    return resVO;
                })
                .toList();
        return R.success(vendors);
    }

    /**
     * 确认发货
     *
     * @param shipVO
     * @return
     * @throws UnsupportedEncodingException
     */
    @PreAuthorize("@ss.hasPermi('shop:order:ship')")
    @PostMapping("ship")
    public R<Boolean> ship(@RequestBody ShipRequestVO shipVO) throws UnsupportedEncodingException {
        iOrderService.ship(shipVO);
        log.info("订单发货完成, orderId={}, shipChannel={}", shipVO.getOrderId(), shipVO.getShipChannel());
        return R.success();
    }

    /**
     * 订单导出
     *
     * @param order
     * @param response
     */
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/export")
    public void export(OrderManagerReqVO order, HttpServletResponse response) {
        Page<Order> page = getPage();
        IPage<OrderManagerResVO> listPage = iOrderService.listPage(page, order);
        List<OrderManagerResVO> records = listPage.getRecords();
        List<OrderExportDTO> list = BeanUtil.copyToList(records, OrderExportDTO.class);
        ExcelUtil.exportExcel(response, list, OrderExportDTO.class, "订单数据.xlsx");
    }
}

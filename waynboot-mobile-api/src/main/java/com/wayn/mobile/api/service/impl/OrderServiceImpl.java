package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.order.WxPayMwebOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Address;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.tool.MailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.task.TaskService;
import com.wayn.common.util.R;
import com.wayn.common.util.ip.IpUtils;
import com.wayn.common.util.mail.MailUtil;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.domain.Order;
import com.wayn.mobile.api.domain.OrderGoods;
import com.wayn.mobile.api.domain.vo.OrderVO;
import com.wayn.mobile.api.mapper.OrderMapper;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.api.service.IOrderGoodsService;
import com.wayn.mobile.api.service.IOrderService;
import com.wayn.mobile.api.task.CancelOrderTask;
import com.wayn.mobile.api.util.OrderHandleOption;
import com.wayn.mobile.api.util.OrderUtil;
import com.wayn.mobile.framework.redis.RedisCache;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IAddressService iAddressService;

    @Autowired
    private ICartService iCartService;

    @Autowired
    private IOrderGoodsService iOrderGoodsService;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private IMemberService iMemberService;

    @Autowired
    private IMailConfigService mailConfigService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TaskService taskService;

    @Override
    public R selectListPage(IPage<Order> page, Integer showType) {
        List<Short> orderStatus = OrderUtil.orderStatus(showType);
        Order order = new Order();
        order.setUserId(SecurityUtils.getUserId());
        IPage<Order> orderIPage = orderMapper.selectOrderListPage(page, order, orderStatus);
        List<Order> orderList = orderIPage.getRecords();
        List<Map<String, Object>> orderVoList = new ArrayList<>(orderList.size());
        for (Order o : orderList) {
            Map<String, Object> orderVo = new HashMap<>();
            orderVo.put("id", o.getId());
            orderVo.put("orderSn", o.getOrderSn());
            orderVo.put("actualPrice", o.getActualPrice());
            orderVo.put("orderStatusText", OrderUtil.orderStatusText(o));
            orderVo.put("handleOption", OrderUtil.build(o));
            orderVo.put("aftersaleStatus", o.getAftersaleStatus());

            List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", o.getId()));
            List<Map<String, Object>> orderGoodsVoList = new ArrayList<>(orderGoodsList.size());
            for (OrderGoods orderGoods : orderGoodsList) {
                Map<String, Object> orderGoodsVo = new HashMap<>();
                orderGoodsVo.put("id", orderGoods.getGoodsId());
                orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
                orderGoodsVo.put("number", orderGoods.getNumber());
                orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
                orderGoodsVo.put("specifications", orderGoods.getSpecifications());
                orderGoodsVo.put("price", orderGoods.getPrice());
                orderGoodsVoList.add(orderGoodsVo);
            }
            orderVo.put("goodsList", orderGoodsVoList);
            orderVoList.add(orderVo);
        }
        return R.success().add("data", orderVoList).add("pages", orderIPage.getPages()).add("page", orderIPage.getCurrent());
    }

    @Override
    public R statusCount() {
        R success = R.success();
        Long userId = SecurityUtils.getUserId();
        List<Order> orderList = list(new QueryWrapper<Order>().select("order_status", "comments").eq("user_id", userId));
        int unpaid = 0;
        int unship = 0;
        int unrecv = 0;
        int uncomment = 0;
        for (Order order : orderList) {
            if (OrderUtil.isCreateStatus(order)) {
                unpaid++;
            } else if (OrderUtil.isPayStatus(order)) {
                unship++;
            } else if (OrderUtil.isShipStatus(order)) {
                unrecv++;
            } else if (OrderUtil.isConfirmStatus(order) || OrderUtil.isAutoConfirmStatus(order)) {
                uncomment += order.getComments();
            }  // todo

        }
        success.add("unpaid", unpaid);
        success.add("unship", unship);
        success.add("unrecv", unrecv);
        success.add("uncomment", uncomment);
        return success;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R submit(OrderVO orderVO) {
        // 验证用户ID，防止用户不一致
        Long userId = orderVO.getUserId();

        if (Objects.isNull(userId) || !userId.equals(SecurityUtils.getUserId())) {
            return R.error("用户ID不一致");
        }
        // 获取用户地址，为空取默认地址
        Long addressId = orderVO.getAddressId();
        Address checkedAddress;
        if (Objects.nonNull(addressId)) {
            checkedAddress = iAddressService.getById(addressId);
        } else {
            checkedAddress = iAddressService.list(new QueryWrapper<Address>().eq("is_default", true)).get(0);
        }

        // 获取用户订单商品，为空默认取购物车已选中商品
        List<Long> cartIdArr = orderVO.getCartIdArr();
        List<Cart> checkedGoodsList;
        if (CollectionUtils.isEmpty(cartIdArr)) {
            checkedGoodsList = iCartService.list(new QueryWrapper<Cart>().eq("checked", true).eq("user_id", userId));
        } else {
            checkedGoodsList = iCartService.listByIds(cartIdArr);
        }

        // 商品费用
        BigDecimal checkedGoodsPrice = new BigDecimal("0.00");
        for (Cart checkGoods : checkedGoodsList) {
            checkedGoodsPrice = checkedGoodsPrice.add(checkGoods.getPrice().multiply(new BigDecimal(checkGoods.getNumber())));
        }

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = new BigDecimal("0.00");
        /*if (checkedGoodsPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
            freightPrice = SystemConfig.getFreight();
        }*/

        // 可以使用的其他钱，例如用户积分
        BigDecimal integralPrice = new BigDecimal("0.00");

        // 优惠卷抵扣费用
        BigDecimal couponPrice = new BigDecimal("0.00");

        // 团购抵扣费用
        BigDecimal grouponPrice = new BigDecimal("0.00");

        // 订单费用
        BigDecimal orderTotalPrice = checkedGoodsPrice.add(freightPrice).subtract(couponPrice).max(new BigDecimal("0.00"));

        // 最终支付费用
        BigDecimal actualPrice = orderTotalPrice.subtract(integralPrice);

        // 组装订单数据
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderSn(OrderUtil.generateOrderSn(userId));
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setConsignee(checkedAddress.getName());
        order.setMobile(checkedAddress.getTel());
        order.setMessage(orderVO.getMessage());
        String detailedAddress = checkedAddress.getProvince() + checkedAddress.getCity() + checkedAddress.getCounty() + " " + checkedAddress.getAddressDetail();
        order.setAddress(detailedAddress);
        order.setFreightPrice(freightPrice);
        order.setCouponPrice(couponPrice);
        order.setGrouponPrice(grouponPrice);
        order.setIntegralPrice(integralPrice);
        order.setGoodsPrice(checkedGoodsPrice);
        order.setOrderPrice(orderTotalPrice);
        order.setActualPrice(actualPrice);
        order.setCreateTime(LocalDateTime.now());
        if (save(order)) {
            Long orderId = order.getId();
            // 添加订单商品表项
            for (Cart cartGoods : checkedGoodsList) {
                // 订单商品
                OrderGoods orderGoods = new OrderGoods();
                orderGoods.setOrderId(orderId);
                orderGoods.setGoodsId(cartGoods.getGoodsId());
                orderGoods.setGoodsSn(cartGoods.getGoodsSn());
                orderGoods.setProductId(cartGoods.getProductId());
                orderGoods.setGoodsName(cartGoods.getGoodsName());
                orderGoods.setPicUrl(cartGoods.getPicUrl());
                orderGoods.setPrice(cartGoods.getPrice());
                orderGoods.setNumber(cartGoods.getNumber());
                orderGoods.setSpecifications(cartGoods.getSpecifications());
                orderGoods.setCreateTime(LocalDateTime.now());
                iOrderGoodsService.save(orderGoods);
            }

            // 删除购物车里面的商品信息
            if (CollectionUtils.isEmpty(cartIdArr)) {
                iCartService.remove(new QueryWrapper<Cart>().eq("user_id", userId));
            } else {
                iCartService.removeByIds(cartIdArr);
            }
            // 商品货品数量减少
            for (Cart checkGoods : checkedGoodsList) {
                Long productId = checkGoods.getProductId();
                GoodsProduct product = iGoodsProductService.getById(productId);
                int remainNumber = product.getNumber() - checkGoods.getNumber();
                if (remainNumber < 0) {
                    throw new RuntimeException("下单的商品货品数量大于库存量");
                }
                if (!iGoodsProductService.reduceStock(productId, checkGoods.getNumber())) {
                    throw new BusinessException("商品货品库存减少失败");
                }
                long delay = 1000; // 一秒
                redisCache.setCacheZset("order_zset", order.getId(), System.currentTimeMillis() + 60 * delay);
                taskService.addTask(new CancelOrderTask(order.getId(), delay * 60));
//                AsyncManager.me().execute(new CancelOrderTask(order.getId()), delay, TimeUnit.MINUTES);
            }
            return R.success().add("orderId", order.getId());
        } else {
            return R.error("订单创建失败");
        }
    }

    @Override
    @Transactional
    public R prepay(Long orderId, HttpServletRequest request) {
        // 获取订单详情
        Order order = getById(orderId);
        String checkMsg = checkOrderOperator(order);
        if (!SysConstants.STRING_TRUE.equals(checkMsg)) {
            return R.error(checkMsg);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return R.error("订单不能支付");
        }
        Member member = iMemberService.getById(SecurityUtils.getUserId());
        String openid = member.getWeixinOpenid();
        if (openid == null) {
            return R.error("订单不能支付");
        }
        WxPayMpOrderResult result;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setOutTradeNo(order.getOrderSn());
            orderRequest.setOpenid(openid);
            orderRequest.setBody("订单：" + order.getOrderSn());
            // 元转成分
            int fee;
            BigDecimal actualPrice = order.getActualPrice();
            fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee(fee);
            orderRequest.setSpbillCreateIp(IpUtils.getIpAddr(request));

            result = wxPayService.createOrder(orderRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("订单不能支付");
        }
        return R.success().add("result", result);
    }


    @Override
    @Transactional
    public R h5pay(Long orderId, HttpServletRequest request) {
        // 获取订单详情
        Order order = getById(orderId);
        String checkMsg = checkOrderOperator(order);
        if (!SysConstants.STRING_TRUE.equals(checkMsg)) {
            return R.error(checkMsg);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return R.error("订单不能支付");
        }

        WxPayMwebOrderResult result;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setOutTradeNo(order.getOrderSn());
            orderRequest.setTradeType("MWEB");
            orderRequest.setBody("订单：" + order.getOrderSn());
            // 元转成分
            int fee;
            BigDecimal actualPrice = order.getActualPrice();
            fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee(fee);
            orderRequest.setSpbillCreateIp(IpUtils.getIpAddr(request));
            result = wxPayService.createOrder(orderRequest);
            return R.success().add("data", result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error("支付失败");
        }
    }

    @Override
    public R payNotify(HttpServletRequest request, HttpServletResponse response) {
        String xmlResult;
        try {
            xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
        } catch (IOException e) {
            e.printStackTrace();
            return R.error(WxPayNotifyResponse.fail(e.getMessage()));
        }

        WxPayOrderNotifyResult result;
        try {
            result = wxPayService.parseOrderNotifyResult(xmlResult);

            if (!WxPayConstants.ResultCode.SUCCESS.equals(result.getResultCode())) {
                log.error(xmlResult);
                throw new WxPayException("微信通知支付失败！");
            }
        } catch (WxPayException e) {
            e.printStackTrace();
            return R.error(WxPayNotifyResponse.fail(e.getMessage()));
        }

        log.info("处理腾讯支付平台的订单支付");
        log.info(result.getReturnMsg());

        String orderSn = result.getOutTradeNo();
        String payId = result.getTransactionId();

        // 分转化成元
        String totalFee = BaseWxPayResult.fenToYuan(result.getTotalFee());
        Order order = getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        if (order == null) {
            return R.error(WxPayNotifyResponse.fail("订单不存在 sn=" + orderSn));
        }

        // 检查这个订单是否已经处理过
        if (OrderUtil.hasPayed(order)) {
            return R.error(WxPayNotifyResponse.success("订单已经处理成功!"));
        }

        // 检查支付订单金额
        if (!totalFee.equals(order.getActualPrice().toString())) {
            return R.error(WxPayNotifyResponse.fail(order.getOrderSn() + " : 支付金额不符合 totalFee=" + totalFee));
        }

        order.setPayId(payId);
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        order.setUpdateTime(LocalDateTime.now());
        if (!updateById(order)) {
            return R.error(WxPayNotifyResponse.fail("更新数据已失效"));
        }

        //TODO 发送邮件和短信通知，这里采用异步发送
        // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
        MailConfig mailConfig = mailConfigService.getById(1L);
        SendMailVO sendMailVO = new SendMailVO();
        sendMailVO.setTitle("新订单通知");
        sendMailVO.setContent(order.toString());
        sendMailVO.setSendMail("1669738430@qq.com");
        MailUtil.sendMail(mailConfig, sendMailVO, false);
        // 删除redis中订单id
        redisCache.deleteZsetObject("order_zset", order.getId());
        // 取消订单超时未支付任务
        taskService.removeTask(new CancelOrderTask(order.getId()));
        return R.error(WxPayNotifyResponse.success("处理成功!"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R cancel(Long orderId) {
        Order order = getById(orderId);
        String checkMsg = checkOrderOperator(order);
        if (!SysConstants.STRING_TRUE.equals(checkMsg)) {
            return R.error(checkMsg);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isCancel()) {
            return R.error("订单不能取消");
        }

        // 设置订单已取消状态
        order.setOrderStatus(OrderUtil.STATUS_CANCEL);
        order.setEndTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if (!updateById(order)) {
            throw new BusinessException("更新数据已失效");
        }

        // 商品货品数量增加
        List<OrderGoods> goodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        for (OrderGoods orderGoods : goodsList) {
            Long productId = orderGoods.getProductId();
            Integer number = orderGoods.getNumber();
            if (!iGoodsProductService.addStock(productId, number)) {
                throw new BusinessException("商品货品库存增加失败");
            }
        }
        // 返还优惠券
//        releaseCoupon(orderId);
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Long orderId) {
        Order order = getById(orderId);
        String checkMsg = checkOrderOperator(order);
        if (!SysConstants.STRING_TRUE.equals(checkMsg)) {
            return R.error(checkMsg);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isDelete()) {
            return R.error("订单不能删除");
        }
        // 删除订单
        removeById(orderId);
        // 删除订单商品
        iOrderGoodsService.remove(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        return R.success();
    }

    @Override
    public R confirm(Long orderId) {
        Order order = getById(orderId);
        String checkMsg = checkOrderOperator(order);
        if (!SysConstants.STRING_TRUE.equals(checkMsg)) {
            return R.error(checkMsg);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isConfirm()) {
            return R.error("订单不能确认收货");
        }
        // 更改订单状态为已收货
        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        return R.success();
    }

    /**
     * 检查订单操作是否合法
     *
     * @param order 订单详情
     * @return 成功返回<code>SysConstants.STRING_TRUE</code>，失败返回<code>SysConstants.STRING_FALSE</code>，或者自定义消息
     */
    private String checkOrderOperator(Order order) {
        Long userId = SecurityUtils.getUserId();
        if (Objects.isNull(order)) {
            return SysConstants.STRING_FALSE;
        }
        if (!order.getUserId().equals(userId)) {
            return SysConstants.STRING_FALSE_MSG("用户ID不一致");
        }
        return SysConstants.STRING_TRUE;
    }
}

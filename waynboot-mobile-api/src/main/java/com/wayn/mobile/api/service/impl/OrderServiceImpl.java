package com.wayn.mobile.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.constant.Constants;
import com.wayn.common.core.domain.shop.*;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.core.util.OrderHandleOption;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.enums.PayTypeEnum;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.task.TaskService;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.common.util.bean.MyBeanUtil;
import com.wayn.common.util.ip.IpUtils;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.messsage.OrderDTO;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.mapper.OrderMapper;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.api.service.IOrderService;
import com.wayn.mobile.api.task.OrderUnpaidTask;
import com.wayn.mobile.api.util.OrderSnGenUtil;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private RedisCache redisCache;
    private IAddressService iAddressService;
    private ICartService iCartService;
    private IOrderGoodsService iOrderGoodsService;
    private IGoodsProductService iGoodsProductService;
    private IGoodsService iGoodsService;
    private WxPayService wxPayService;
    private IMemberService iMemberService;
    private OrderMapper orderMapper;
    private TaskService taskService;
    private IMailService iMailService;
    private RabbitTemplate rabbitTemplate;
    private AlipayConfig alipayConfig;

    @Override
    public R selectListPage(IPage<Order> page, Integer showType) {
        List<Short> orderStatus = OrderUtil.orderStatus(showType);
        Order order = new Order();
        order.setUserId(MobileSecurityUtils.getUserId());
        IPage<Order> orderIPage = orderMapper.selectOrderListPage(page, order, orderStatus);
        List<Order> orderList = orderIPage.getRecords();
        List<Map<String, Object>> orderVoList = new ArrayList<>(orderList.size());
        List<Long> idList = orderList.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderGoods>> orderGoodsListMap = iOrderGoodsService
                .list(Wrappers.lambdaQuery(OrderGoods.class).in(CollectionUtils.isNotEmpty(idList), OrderGoods::getOrderId, idList))
                .stream().collect(Collectors.groupingBy(OrderGoods::getOrderId));

        for (Order o : orderList) {
            Map<String, Object> orderVo = new HashMap<>();
            orderVo.put("id", o.getId());
            orderVo.put("orderSn", o.getOrderSn());
            orderVo.put("actualPrice", o.getActualPrice());
            orderVo.put("orderStatusText", OrderUtil.orderStatusText(o));
            orderVo.put("handleOption", OrderUtil.build(o));
            orderVo.put("aftersaleStatus", o.getAftersaleStatus());

            List<OrderGoods> orderGoodsList = orderGoodsListMap.get(o.getId());
            List<Map<String, Object>> orderGoodsVoList = new ArrayList<>(orderGoodsList.size());
            for (OrderGoods orderGoods : orderGoodsList) {
                Map<String, Object> orderGoodsVo = new HashMap<>();
                orderGoodsVo.put("id", orderGoods.getId());
                orderGoodsVo.put("goodsId", orderGoods.getGoodsId());
                orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
                orderGoodsVo.put("number", orderGoods.getNumber());
                orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
                orderGoodsVo.put("specifications", orderGoods.getSpecifications());
                orderGoodsVo.put("price", orderGoods.getPrice());
                orderGoodsVo.put("comment", orderGoods.getComment());
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
        Long userId = MobileSecurityUtils.getUserId();
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
    public R asyncSubmit(OrderVO orderVO) {
        OrderDTO orderDTO = new OrderDTO();
        MyBeanUtil.copyProperties(orderVO, orderDTO);
        Long userId = orderDTO.getUserId();

        // 获取用户订单商品，为空默认取购物车已选中商品
        List<Long> cartIdArr = orderDTO.getCartIdArr();
        List<Cart> checkedGoodsList;
        if (CollectionUtils.isEmpty(cartIdArr)) {
            checkedGoodsList = iCartService.list(new QueryWrapper<Cart>().eq("checked", true).eq("user_id", userId));
        } else {
            checkedGoodsList = iCartService.listByIds(cartIdArr);
        }
        List<Long> goodsIds = checkedGoodsList.stream().map(Cart::getGoodsId).collect(Collectors.toList());
        List<GoodsProduct> goodsProducts = iGoodsProductService.list(new QueryWrapper<GoodsProduct>().in("goods_id", goodsIds));
        Map<Long, GoodsProduct> goodsIdMap = goodsProducts.stream().collect(
                Collectors.toMap(GoodsProduct::getId, goodsProduct -> goodsProduct));
        // 商品货品数量减少
        for (Cart checkGoods : checkedGoodsList) {
            Long productId = checkGoods.getProductId();
            Long goodsId = checkGoods.getGoodsId();
            GoodsProduct product = goodsIdMap.get(productId);
            int remainNumber = product.getNumber() - checkGoods.getNumber();
            if (remainNumber < 0) {
                Goods goods = iGoodsService.getById(goodsId);
                String goodsName = goods.getName();
                String[] specifications = product.getSpecifications();
                throw new BusinessException(String.format("%s,%s 库存不足", goodsName, StringUtils.join(specifications, " ")));
            }
            if (!iGoodsProductService.reduceStock(productId, checkGoods.getNumber())) {
                throw new BusinessException("商品货品库存减少失败");
            }
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
        String orderSn = OrderSnGenUtil.generateOrderSn(userId);
        orderDTO.setOrderSn(orderSn);

        // 异步下单
        String uid = IdUtil.getUid();
        System.out.println(uid);
        CorrelationData correlationData = new CorrelationData(uid);
        Map<String, Object> map = new HashMap<>();
        map.put("order", orderDTO);
        map.put("notifyUrl", WaynConfig.getMobileUrl() + "/message/order/submit");
        try {
            Message message = MessageBuilder
                    .withBody(JSON.toJSONString(map).getBytes(Constants.UTF_ENCODING))
                    .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend("OrderDirectExchange", "OrderDirectRouting", message, correlationData);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return R.success().add("actualPrice", actualPrice).add("orderSn", orderSn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R submit(OrderDTO orderDTO) {
        Long userId = orderDTO.getUserId();

        // 获取用户地址
        Long addressId = orderDTO.getAddressId();
        Address checkedAddress;
        if (Objects.isNull(addressId)) {
            throw new BusinessException("收获地址为空，请求参数" + JSON.toJSONString(orderDTO));
        }
        checkedAddress = iAddressService.getById(addressId);

        // 获取用户订单商品，为空默认取购物车已选中商品
        List<Long> cartIdArr = orderDTO.getCartIdArr();
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
        order.setOrderSn(orderDTO.getOrderSn());
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setConsignee(checkedAddress.getName());
        order.setMobile(checkedAddress.getTel());
        order.setMessage(orderDTO.getMessage());
        String detailedAddress = checkedAddress.getProvince() + checkedAddress.getCity() + checkedAddress.getCounty() + " " + checkedAddress.getAddressDetail();
        order.setAddress(detailedAddress);
        order.setFreightPrice(freightPrice);
        order.setCouponPrice(couponPrice);
        order.setGrouponPrice(grouponPrice);
        order.setIntegralPrice(integralPrice);
        order.setGoodsPrice(checkedGoodsPrice);
        order.setOrderPrice(orderTotalPrice);
        order.setActualPrice(actualPrice);
        order.setCreateTime(new Date());
        if (!save(order)) {
            throw new BusinessException("订单创建失败" + JSON.toJSONString(order));
        }

        Long orderId = order.getId();
        List<OrderGoods> orderGoodsList = new ArrayList<>();
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
            orderGoodsList.add(orderGoods);
        }
        if (!iOrderGoodsService.saveBatch(orderGoodsList)) {
            throw new BusinessException("添加订单商品表项失败" + JSON.toJSONString(orderGoodsList));
        }

        // 删除购物车里面的商品信息
        if (CollectionUtils.isEmpty(cartIdArr)) {
            iCartService.remove(new QueryWrapper<Cart>().eq("user_id", userId));
        } else {
            iCartService.removeByIds(cartIdArr);
        }
        // 下单60s内未支付自动取消订单
        long delay = 1000;
        redisCache.setCacheZset("order_zset", order.getId(), System.currentTimeMillis() + 60 * delay);
        taskService.addTask(new OrderUnpaidTask(order.getId(), delay * 60));
        return R.success().add("orderId", order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R prepay(String orderSn, Integer payType, HttpServletRequest request) {
        // 获取订单详情
        Order order = getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!returnCodeEnum.equals(ReturnCodeEnum.SUCCESS)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        // 设置支付方式
        order.setPayType(payType);
        Member member = iMemberService.getById(MobileSecurityUtils.getUserId());
        String openid = member.getWeixinOpenid();
        if (openid == null) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
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
            return R.success().add("result", result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R h5pay(String orderSn, Integer payType, HttpServletRequest request) {
        // 获取订单详情
        Order order = getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        Long userId = order.getUserId();
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        // 保存支付方式
        boolean update = lambdaUpdate()
                .set(Order::getPayType, payType)
                .eq(Order::getOrderSn, orderSn).update();
        if (!update) {
            return R.error(ReturnCodeEnum.ORDER_SET_PAY_ERROR);
        }
        switch (Objects.requireNonNull(PayTypeEnum.of(payType))) {
            case WX:
                WxPayMpOrderResult result;
                try {
                    WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
                    orderRequest.setOutTradeNo(order.getOrderSn());
                    orderRequest.setTradeType(WxPayConstants.TradeType.MWEB);
                    orderRequest.setBody("订单：" + order.getOrderSn());
                    // 元转成分
                    int fee;
                    BigDecimal actualPrice = order.getActualPrice();
                    fee = actualPrice.multiply(new BigDecimal(100)).intValue();
                    orderRequest.setTotalFee(fee);
                    orderRequest.setSpbillCreateIp(IpUtils.getIpAddr(request));

                    result = wxPayService.createOrder(orderRequest);
                    return R.success().add("result", result);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
                }
            case ALI:
                // 初始化
                AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                        alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                        alipayConfig.getSigntype());
                // 创建API对应的request，使用手机网站支付request
                AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
                // 在公共参数中设置回跳和通知地址
                String url = WaynConfig.getMobileUrl() + request.getContextPath();
                alipayRequest.setReturnUrl(url + "/returnOrders/" + orderSn + "/" + userId);
                alipayRequest.setNotifyUrl(url + "/paySuccess?payType=1&orderSn=" + orderSn);

                // 填充业务参数
                // 必填
                // 商户订单号，需保证在商户端不重复
                String out_trade_no = orderSn + new Random().nextInt(9999);
                // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
                String product_code = "FAST_INSTANT_TRADE_PAY";
                // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
                BigDecimal actualPrice = order.getActualPrice();
                String total_amount = actualPrice.toString();
                // 订单标题
                String subject = "支付宝测试";

                // 选填
                // 商品描述，可空
                String body = "商品描述";
                alipayRequest.setBizContent("{" + "\"out_trade_no\":\"" + out_trade_no + "\"," + "\"product_code\":\""
                        + product_code + "\"," + "\"total_amount\":\"" + total_amount + "\"," + "\"subject\":\"" + subject
                        + "\"," + "\"body\":\"" + body + "\"}");
                // 请求
                String form;
                try {
                    // 需要自行申请支付宝的沙箱账号、申请appID，并在配置文件中依次配置AppID、密钥、公钥，否则这里会报错。
                    form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
                    return R.success().add("form", form);
                } catch (AlipayApiException e) {
                    log.error(e.getMessage(), e);
                    return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
                }
            case ALI_TEST:
                // 支付宝test，直接更新支付状态为已支付
                order.setPayId("xxxxx0987654321-ali");
                order.setPayTime(LocalDateTime.now());
                order.setOrderStatus(OrderUtil.STATUS_PAY);
                order.setUpdateTime(new Date());
                if (!updateById(order)) {
                    return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
                }

                // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
                String email = iMemberService.getById(order.getUserId()).getEmail();
                if (StringUtils.isNotBlank(email)) {
                    iMailService.sendEmail("新订单通知", order.toString(), email, WaynConfig.getMobileUrl() + "/message/email");
                }

                // 删除redis中订单id
                redisCache.deleteZsetObject("order_zset", order.getId());
                // 取消订单超时未支付任务
                taskService.removeTask(new OrderUnpaidTask(order.getId()));
                return R.success();
            default:
                return R.error(ReturnCodeEnum.ORDER_NOT_SUPPORT_PAYWAY_ERROR);
        }
    }

    @Override
    public void wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        String xmlResult = null;
        try {
            xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
        } catch (IOException e) {
            log.error(WxPayNotifyResponse.fail(e.getMessage()), e);
        }

        WxPayOrderNotifyResult result = null;
        try {
            result = wxPayService.parseOrderNotifyResult(xmlResult);

            if (!WxPayConstants.ResultCode.SUCCESS.equals(result.getReturnCode())) {
                log.error(xmlResult);
            }
        } catch (WxPayException e) {
            log.error(e.getMessage(), e);
        }

        log.info("处理腾讯支付平台的订单支付, {}", result.getReturnMsg());

        String orderSn = result.getOutTradeNo();
        String payId = result.getTransactionId();

        // 分转化成元
        String totalFee = BaseWxPayResult.fenToYuan(result.getTotalFee());
        Order order = getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        if (order == null) {
            log.error("微信支付回调：订单不存在，orderSn：{}", orderSn);
            return;
        }

        // 检查这个订单是否已经处理过
        if (OrderUtil.hasPayed(order)) {
            log.error("微信支付回调：订单已经处理过了，orderSn：{}", orderSn);
            return;
        }

        // 检查支付订单金额
        if (!totalFee.equals(order.getActualPrice().toString())) {
            log.error("微信支付回调: 支付金额不符合，orderSn：{}，totalFee：{}", order.getOrderSn(), totalFee);
            return;
        }

        order.setPayId(payId);
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        order.setUpdateTime(new Date());
        if (!updateById(order)) {
            log.error("微信支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
            return;
        }

        // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
        String email = iMemberService.getById(order.getUserId()).getEmail();
        if (StringUtils.isNotBlank(email)) {
            iMailService.sendEmail("新订单通知", order.toString(), email, WaynConfig.getMobileUrl() + "/message/email");
        }
        // 删除redis中订单id
        redisCache.deleteZsetObject("order_zset", order.getId());
        // 取消订单超时未支付任务
        taskService.removeTask(new OrderUnpaidTask(order.getId()));
    }

    @Override
    public void aliPayNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException {
        //将异步通知中收到的所有参数都存放到map中
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> paramsMap = new HashMap<>();
        parameterMap.forEach((s, strings) -> {
            paramsMap.put(s, strings[0]);
        });
        // 调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSigntype());
        if (!signVerified) {
            log.error("支付宝支付回调：验签失败");
            return;
        }
        log.info("支付宝支付回调：开始");
        // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
        String orderSn = request.getParameter("orderSn");
        Order order = getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        if (order == null) {
            log.error("支付宝支付回调：订单不存在，orderSn：{}", orderSn);
            return;
        }

        // 检查这个订单是否已经处理过
        if (OrderUtil.hasPayed(order)) {
            log.error("支付宝支付回调：订单已经处理过了，orderSn：{}", orderSn);
            return;
        }

        order.setPayId("0xsdfsadfas-ali");
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        order.setUpdateTime(new Date());
        if (!updateById(order)) {
            log.error("支付宝支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
            return;
        }

        // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
        String email = iMemberService.getById(order.getUserId()).getEmail();
        if (StringUtils.isNotBlank(email)) {
            iMailService.sendEmail("新订单通知", order.toString(), email, WaynConfig.getMobileUrl() + "/message/email");
        }
        // 删除redis中订单id
        redisCache.deleteZsetObject("order_zset", order.getId());
        // 取消订单超时未支付任务
        taskService.removeTask(new OrderUnpaidTask(order.getId()));
        log.info("支付宝支付回调：结束");
    }

    @Override
    public R searchResult(String orderSn) {
        Order order = getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        if (order == null) {
            return R.error(ReturnCodeEnum.ORDER_NOT_EXISTS_ERROR);
        }

        // 检查这个订单是否已经处理过
        if (!OrderUtil.isCreateStatus(order)) {
            return R.error(ReturnCodeEnum.ORDER_HAS_CREATED_ERROR);
        }
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R cancel(Long orderId) {
        Order order = getById(orderId);
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isCancel()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_CANCAL_ERROR);
        }

        // 设置订单已取消状态
        order.setOrderStatus(OrderUtil.STATUS_CANCEL);
        order.setOrderEndTime(LocalDateTime.now());
        order.setUpdateTime(new Date());
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
        // releaseCoupon(orderId);
        return R.success();
    }

    @Override
    public R refund(Long orderId) {
        Order order = getById(orderId);
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }

        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isRefund()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        }

        // 设置订单申请退款状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND);
        order.setUpdateTime(new Date());
        updateById(order);

        // 有用户申请退款，邮件通知运营人员
        String email = iMemberService.getById(order.getUserId()).getEmail();
        if (StringUtils.isNotEmpty(email)) {
            if (StringUtils.isNotBlank(email)) {
                iMailService.sendEmail("订单正在退款", order.toString(), email, WaynConfig.getMobileUrl() + "/message/email");
            }
        }

        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Long orderId) {
        Order order = getById(orderId);
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isDelete()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_DELETE_ERROR);
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
        ReturnCodeEnum returnCodeEnum = checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isConfirm()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_CONFIRM_ERROR);
        }
        // 更改订单状态为已收货
        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setConfirmTime(LocalDateTime.now());
        order.setUpdateTime(new Date());
        updateById(order);
        return R.success();
    }

    /**
     * 检查订单操作是否合法
     *
     * @param order 订单详情
     * @return 成功返回<code>SysConstants.STRING_TRUE</code>，失败返回<code>SysConstants.STRING_FALSE</code>，或者自定义消息
     */
    private ReturnCodeEnum checkOrderOperator(Order order) {
        Long userId = MobileSecurityUtils.getUserId();
        if (Objects.isNull(order)) {
            return ReturnCodeEnum.USER_NOT_EXISTS_ERROR;
        }
        if (!order.getUserId().equals(userId)) {
            return ReturnCodeEnum.ORDER_USER_NOT_SAME_ERROR;
        }
        return ReturnCodeEnum.SUCCESS;
    }

    /*public void releaseCoupon(Long orderId) {
        List<CouponUser> couponUserList = couponUserService.list(new QueryWrapper<CouponUser>().eq("order_id", orderId));
        if (CollectionUtils.isEmpty(couponUserList)) {
            return;
        }
        for (CouponUser couponUser : couponUserList) {
            couponUser.setStatus((byte) 0);
            couponUser.setUpdateTime(new Date());
            couponUserService.updateById(couponUser);
        }
    }*/
}

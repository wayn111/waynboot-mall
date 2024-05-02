package com.wayn.test;

import com.wayn.common.core.service.shop.IPayService;
import com.wayn.common.request.OrderPayReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.wayn.common.design.strategy.pay.PayTypeEnum.EPAY_MOBILE;

/**
 * @author: waynaqua
 * @date: 2024/4/30 16:18
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PayTest {

    @Resource
    private IPayService payService;

    /**
     * 易支付下单测试
     */
    @Test
    public void epayPreapreTest() {
        OrderPayReqVO orderPayReqVO = new OrderPayReqVO();
        orderPayReqVO.setOrderSn("1702220024000013");
        orderPayReqVO.setPayType(EPAY_MOBILE.getType());
        orderPayReqVO.setReturnUrl("http://www.baidu.com");
        payService.prepay(orderPayReqVO);
    }
}

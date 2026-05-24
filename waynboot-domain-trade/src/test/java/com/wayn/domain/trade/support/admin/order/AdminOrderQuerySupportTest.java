package com.wayn.domain.trade.support.admin.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.mapper.AdminOrderMapper;
import com.wayn.domain.api.trade.service.IMemberService;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.trade.request.OrderManagerReqVO;
import com.wayn.domain.api.trade.response.OrderDetailResVO;
import com.wayn.domain.api.trade.response.OrderManagerResVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.RefundStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderQuerySupportTest {

    @Mock
    private AdminOrderMapper adminOrderMapper;
    @Mock
    private IOrderGoodsService orderGoodsService;
    @Mock
    private IMemberService memberService;

    @Test
    void listPageFillsStatusMessages() {
        AdminOrderQuerySupport support = new AdminOrderQuerySupport(adminOrderMapper, orderGoodsService, memberService);
        Page<Order> page = new Page<>(1, 10);
        OrderManagerReqVO reqVO = new OrderManagerReqVO();
        OrderManagerResVO record = new OrderManagerResVO();
        record.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
        record.setRefundStatus(RefundStatusEnum.APPLY_REFUND.getStatus());
        record.setPayType(1);
        record.setRefundType(2);
        IPage<OrderManagerResVO> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of(record));

        when(adminOrderMapper.selectOrderListPage(page, reqVO)).thenReturn(resultPage);

        IPage<OrderManagerResVO> result = support.listPage(page, reqVO);

        assertEquals("已付款", result.getRecords().get(0).getOrderStatusMsg());
        assertEquals("申请退款", result.getRecords().get(0).getRefundStatusMsg());
        assertEquals("微信H5", result.getRecords().get(0).getPayTypeMsg());
        assertEquals("支付宝H5", result.getRecords().get(0).getRefundTypeMsg());
    }

    @Test
    void detailBuildsOrderDetailResponse() {
        AdminOrderQuerySupport support = new AdminOrderQuerySupport(adminOrderMapper, orderGoodsService, memberService);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L);
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
        order.setRefundStatus(RefundStatusEnum.APPLY_REFUND.getStatus());
        order.setPayType(1);
        order.setRefundType(2);
        Member member = new Member();
        member.setId(2L);
        member.setNickname("tester");
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrderId(1L);

        when(adminOrderMapper.selectById(1L)).thenReturn(order);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(orderGoods));
        when(memberService.getById(2L)).thenReturn(member);

        OrderDetailResVO result = support.detail(1L);

        assertEquals(OrderStatusEnum.STATUS_PAY.getStatus(), result.getOrder().getOrderStatus());
        assertEquals("已付款", result.getOrder().getOrderStatusMsg());
        assertEquals(1, result.getOrderGoods().size());
        assertEquals("tester", result.getUser().getNickname());
    }
}

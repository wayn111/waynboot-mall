package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.ShipVO;
import com.wayn.common.util.R;

/**
 * <p>
 * 类目表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface IAdminOrderService extends IService<Order> {

    /**
     * 获取订单列表
     *
     * @param page 分页对象
     * @param order 订单对象
     * @return r
     */
    IPage<Order> listPage(IPage<Order> page, Order order);

    /**
     * 订单退款
     * <p>
     * 1. 检测当前订单是否能够退款;
     * 2. 微信退款操作;
     * 3. 设置订单退款确认状态；
     * 4. 订单商品库存回库。
     * <p>
     * TODO
     * 虽然接入了微信退款API，但是从安全角度考虑，建议开发者删除这里微信退款代码，采用以下两步走步骤：
     * 1. 管理员登录微信官方支付平台点击退款操作进行退款
     * 2. 管理员登录litemall管理后台点击退款操作进行订单状态修改和商品库存回库
     *
     * @param orderId 订单id
     * @return r
     */
    R refund(Long orderId);

    /**
     * 发货
     * 1. 检测当前订单是否能够发货
     * 2. 设置订单发货状态
     *
     * @param shipVO 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
     * @return r
     */
    R ship(ShipVO shipVO);

    /**
     * 获取订单详情（包含订单信息，订单商品信息，用户信息）
     * @param orderId
     * @return r
     */
    R detail(Long orderId);
}

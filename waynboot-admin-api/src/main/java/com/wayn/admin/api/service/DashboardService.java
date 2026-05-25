package com.wayn.admin.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.model.response.DashboardChannelVO;
import com.wayn.common.model.response.DashboardMemberTrendVO;
import com.wayn.common.model.response.DashboardPeriodVO;
import com.wayn.common.model.response.DashboardRecentVO;
import com.wayn.common.model.response.DashboardStatsVO;
import com.wayn.common.model.response.DashboardTopGoodsVO;
import com.wayn.common.model.response.DashboardTrendVO;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.enums.PayTypeEnum;
import com.wayn.domain.api.trade.mapper.AdminOrderMapper;
import com.wayn.domain.api.trade.mapper.MemberMapper;
import com.wayn.domain.api.trade.service.IMemberService;
import com.wayn.domain.api.trade.service.IOrderService;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端数据看板统计服务。
 * <p>
 * 该服务承接 Controller 的查询编排职责，统一封装订单、会员、商品和库存统计口径，
 * 避免入口层直接散落 SQL 聚合、状态分组和 VO 组装逻辑。
 */
@Service
@AllArgsConstructor
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int TOP_GOODS_LIMIT = 5;
    private static final int RECENT_LIMIT = 5;
    private static final int TREND_DAYS = 7;
    private static final int MEMBER_TREND_DAYS = 30;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private static final List<Short> PAID_LIFECYCLE_STATUSES = List.of(
            OrderStatusEnum.STATUS_PAY.getStatus(),
            OrderStatusEnum.STATUS_REFUND.getStatus(),
            OrderStatusEnum.STATUS_SHIP.getStatus(),
            OrderStatusEnum.STATUS_CONFIRM.getStatus(),
            OrderStatusEnum.STATUS_AUTO_CONFIRM.getStatus()
    );
    private static final String PAID_STATUS_SQL = PAID_LIFECYCLE_STATUSES.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    private final IOrderService orderService;
    private final IMemberService memberService;
    private final IGoodsService goodsService;
    private final IGoodsProductService goodsProductService;
    private final AdminOrderMapper adminOrderMapper;
    private final MemberMapper memberMapper;

    /**
     * 查询看板核心指标。
     * <p>
     * 订单数量按业务状态分组统计；销售额和支付转化率只纳入已支付生命周期订单，
     * 避免未支付、取消和退款完成订单污染经营数据。
     *
     * @return 看板核心统计指标
     */
    public DashboardStatsVO stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        DashboardStatsVO vo = new DashboardStatsVO();

        long todayOrders = orderService.count(buildOrderWrapper(todayStart, null));
        long todayPaidOrders = orderService.count(buildOrderWrapper(todayStart, null)
                .in(Order::getOrderStatus, PAID_LIFECYCLE_STATUSES));

        vo.setMemberCount(memberService.count());
        vo.setTodayMemberCount(memberService.count(
                Wrappers.lambdaQuery(Member.class).ge(Member::getCreateTime, todayStart)));
        vo.setTodayOrderCount(todayOrders);
        vo.setTotalOrderCount(orderService.count());
        vo.setConversionRate(calculateGrowthRate(todayPaidOrders, todayOrders, 1));

        vo.setPendingPayCount(countOrderByStatus(OrderStatusEnum.STATUS_CREATE));
        vo.setPendingShipCount(countOrderByStatus(OrderStatusEnum.STATUS_PAY));
        vo.setPendingReceiveCount(countOrderByStatus(OrderStatusEnum.STATUS_SHIP));
        vo.setCompletedOrderCount(orderService.count(Wrappers.lambdaQuery(Order.class)
                .in(Order::getOrderStatus,
                        OrderStatusEnum.STATUS_CONFIRM.getStatus(),
                        OrderStatusEnum.STATUS_AUTO_CONFIRM.getStatus())));
        vo.setClosedOrderCount(orderService.count(Wrappers.lambdaQuery(Order.class)
                .in(Order::getOrderStatus,
                        OrderStatusEnum.STATUS_CANCEL.getStatus(),
                        OrderStatusEnum.STATUS_AUTO_CANCEL.getStatus())));
        vo.setRefundCount(countOrderByStatus(OrderStatusEnum.STATUS_REFUND));

        vo.setOnSaleGoodsCount(goodsService.count(
                Wrappers.lambdaQuery(Goods.class).eq(Goods::getIsOnSale, true)));
        vo.setLowStockCount(goodsProductService.count(
                Wrappers.lambdaQuery(GoodsProduct.class).le(GoodsProduct::getNumber, LOW_STOCK_THRESHOLD)));

        vo.setTotalSales(querySales(buildSalesWrapper(null, null)));
        vo.setTodaySales(querySales(buildSalesWrapper(todayStart, null)));
        return vo;
    }

    /**
     * 查询近 7 日销售趋势。
     * <p>
     * 每天保留订单数和已支付销售额，缺失日期补零，保证前端折线图横轴稳定。
     *
     * @return 近 7 日趋势数据
     */
    public DashboardTrendVO trend() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(TREND_DAYS - 1L).atStartOfDay();
        List<Map<String, Object>> rows = adminOrderMapper.selectMaps(
                Wrappers.<Order>query().ge("create_time", start)
                        .select("DATE(create_time) AS day",
                                "COUNT(*) AS orderCount",
                                "SUM(CASE WHEN order_status IN (" + PAID_STATUS_SQL
                                        + ") THEN actual_price ELSE 0 END) AS sales")
                        .groupBy("DATE(create_time)").orderByAsc("day"));
        Map<String, Map<String, Object>> byDay = rows.stream()
                .collect(Collectors.toMap(row -> getRowString(row, "day"), row -> row, (left, right) -> left));

        List<String> dates = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> row = byDay.get(date.toString());
            dates.add(date.format(DATE_FMT));
            orderCounts.add(toLong(getRowValue(row, "orderCount"), 0L));
            salesList.add(toBigDecimal(getRowValue(row, "sales")));
        }
        return new DashboardTrendVO(dates, orderCounts, salesList);
    }

    /**
     * 查询今日、本周、本月的订单和销售额环比。
     * <p>
     * 当前周期结束时间为空表示统计到当前时刻；历史周期使用左闭右开区间，避免边界重复。
     *
     * @return 周期统计数据
     */
    public DashboardPeriodVO period() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        DashboardPeriodVO vo = new DashboardPeriodVO();
        vo.setToday(buildPeriodItem(todayStart, null, todayStart.minusDays(1), todayStart));
        vo.setWeek(buildPeriodItem(weekStart, null, weekStart.minusWeeks(1), weekStart));
        vo.setMonth(buildPeriodItem(monthStart, null, monthStart.minusMonths(1), monthStart));
        return vo;
    }

    /**
     * 查询支付渠道订单分布。
     * <p>
     * 只统计已进入支付生命周期的订单，渠道名优先使用支付枚举描述，未知渠道归入“其他”。
     *
     * @return 支付渠道统计列表
     */
    public List<DashboardChannelVO> paymentChannel() {
        List<Map<String, Object>> rows = adminOrderMapper.selectMaps(
                Wrappers.<Order>query().in("order_status", PAID_LIFECYCLE_STATUSES)
                        .select("pay_type", "COUNT(*) AS cnt", "SUM(actual_price) AS sales")
                        .groupBy("pay_type"));
        return rows.stream()
                .map(row -> new DashboardChannelVO(
                        resolveChannelName(getRowValue(row, "pay_type")),
                        toLong(getRowValue(row, "cnt"), 0L),
                        toBigDecimal(getRowValue(row, "sales"))))
                .collect(Collectors.toList());
    }

    /**
     * 查询热销商品列表。
     * <p>
     * 商品按已支付订单明细聚合销量倒序取前 5 个，库存使用该商品所有 SKU 的最小可售库存。
     * 商品表的 actualSales 偏展示维护口径，不能作为看板真实热销榜的数据来源。
     *
     * @return 热销商品统计列表
     */
    public List<DashboardTopGoodsVO> topGoods() {
        List<Map<String, Object>> salesRows = adminOrderMapper.selectTopGoodsByPaidOrders(
                PAID_LIFECYCLE_STATUSES, TOP_GOODS_LIMIT);
        if (CollectionUtils.isEmpty(salesRows)) {
            return List.of();
        }

        Map<Long, Integer> salesMap = buildTopGoodsSalesMap(salesRows);
        if (salesMap.isEmpty()) {
            return List.of();
        }
        List<Long> goodsIds = new ArrayList<>(salesMap.keySet());
        Map<Long, Goods> goodsMap = goodsService.list(Wrappers.lambdaQuery(Goods.class)
                        .in(Goods::getId, goodsIds))
                .stream()
                .collect(Collectors.toMap(Goods::getId, goods -> goods, (left, right) -> left));
        Map<Long, Integer> stockMap = queryMinimumSkuStock(goodsIds);

        return goodsIds.stream()
                .filter(goodsMap::containsKey)
                .map(goodsMap::get)
                .map(goods -> buildTopGoods(goods,
                        salesMap.getOrDefault(goods.getId(), 0),
                        stockMap.getOrDefault(goods.getId(), 0)))
                .collect(Collectors.toList());
    }

    /**
     * 查询低库存商品列表。
     * <p>
     * 先按 SKU 可售库存升序取库存最低的货品，再聚合到商品维度，避免库存预警继续复用热销榜数据。
     *
     * @return 低库存商品列表
     */
    public List<DashboardTopGoodsVO> lowStockGoods() {
        List<GoodsProduct> products = goodsProductService.list(
                Wrappers.lambdaQuery(GoodsProduct.class)
                        .le(GoodsProduct::getNumber, LOW_STOCK_THRESHOLD)
                        .orderByAsc(GoodsProduct::getNumber)
                        .last("limit " + TOP_GOODS_LIMIT * 3));
        if (CollectionUtils.isEmpty(products)) {
            return List.of();
        }

        Map<Long, Integer> stockMap = products.stream()
                .collect(Collectors.toMap(
                        GoodsProduct::getGoodsId,
                        product -> product.getNumber() == null ? 0 : product.getNumber(),
                        Integer::min));
        List<Long> goodsIds = products.stream()
                .map(GoodsProduct::getGoodsId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(goodsIds)) {
            return List.of();
        }

        Map<Long, Goods> goodsMap = goodsService.list(Wrappers.lambdaQuery(Goods.class)
                        .in(Goods::getId, goodsIds))
                .stream()
                .collect(Collectors.toMap(Goods::getId, goods -> goods, (left, right) -> left));
        return goodsIds.stream()
                .filter(goodsMap::containsKey)
                .map(goodsMap::get)
                .map(goods -> buildTopGoods(goods,
                        goods.getActualSales() == null ? 0 : goods.getActualSales(),
                        stockMap.getOrDefault(goods.getId(), 0)))
                .sorted(java.util.Comparator.comparing(DashboardTopGoodsVO::getStock))
                .limit(TOP_GOODS_LIMIT)
                .collect(Collectors.toList());
    }

    /**
     * 查询近 30 日会员增长趋势。
     * <p>
     * 缺失日期补零，保证前端会员趋势图横轴连续。
     *
     * @return 会员增长趋势数据
     */
    public DashboardMemberTrendVO memberTrend() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(MEMBER_TREND_DAYS - 1L).atStartOfDay();
        List<Map<String, Object>> rows = memberMapper.selectMaps(
                Wrappers.<Member>query().ge("create_time", start)
                        .select("DATE(create_time) AS day", "COUNT(*) AS cnt")
                        .groupBy("DATE(create_time)").orderByAsc("day"));
        Map<String, Long> byDay = rows.stream()
                .collect(Collectors.toMap(
                        row -> getRowString(row, "day"),
                        row -> toLong(getRowValue(row, "cnt"), 0L),
                        (left, right) -> left));

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (int i = MEMBER_TREND_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(DATE_FMT));
            counts.add(byDay.getOrDefault(date.toString(), 0L));
        }
        return new DashboardMemberTrendVO(dates, counts);
    }

    /**
     * 查询最近订单和最近会员。
     * <p>
     * 该接口保留现有对象式返回契约，分别提供最近订单和最近会员列表。
     *
     * @return 最近动态数据
     */
    public DashboardRecentVO recent() {
        List<Order> orders = orderService.list(
                Wrappers.lambdaQuery(Order.class).orderByDesc(Order::getCreateTime).last("limit " + RECENT_LIMIT));
        List<Member> members = memberService.list(
                Wrappers.lambdaQuery(Member.class).orderByDesc(Member::getCreateTime).last("limit " + RECENT_LIMIT));
        List<DashboardRecentVO.RecentOrderItem> orderItems = orders.stream()
                .map(order -> new DashboardRecentVO.RecentOrderItem(
                        order.getOrderSn(),
                        order.getActualPrice(),
                        order.getCreateTime(),
                        order.getOrderStatus()))
                .collect(Collectors.toList());
        List<DashboardRecentVO.RecentMemberItem> memberItems = members.stream()
                .map(member -> new DashboardRecentVO.RecentMemberItem(
                        member.getNickname(),
                        member.getMobile(),
                        member.getCreateTime()))
                .collect(Collectors.toList());
        return new DashboardRecentVO(orderItems, memberItems);
    }

    /**
     * 按单个订单状态统计订单数。
     *
     * @param statusEnum 订单状态枚举
     * @return 订单数
     */
    private long countOrderByStatus(OrderStatusEnum statusEnum) {
        return orderService.count(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderStatus, statusEnum.getStatus()));
    }

    /**
     * 构建订单时间区间查询条件。
     *
     * @param start 开始时间，不能为空
     * @param end   结束时间，允许为空
     * @return 订单查询条件
     */
    private LambdaQueryWrapper<Order> buildOrderWrapper(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery(Order.class).ge(Order::getCreateTime, start);
        if (end != null) {
            wrapper.lt(Order::getCreateTime, end);
        }
        return wrapper;
    }

    /**
     * 构建销售额聚合查询条件。
     *
     * @param start 开始时间，允许为空
     * @param end   结束时间，允许为空
     * @return 销售额聚合查询条件
     */
    private QueryWrapper<Order> buildSalesWrapper(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<Order> wrapper = Wrappers.<Order>query()
                .in("order_status", PAID_LIFECYCLE_STATUSES)
                .select("SUM(actual_price) AS total");
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        return wrapper;
    }

    /**
     * 查询销售额合计。
     *
     * @param wrapper 销售额聚合查询条件
     * @return 销售额合计，空结果返回 0
     */
    private BigDecimal querySales(QueryWrapper<Order> wrapper) {
        List<Map<String, Object>> rows = adminOrderMapper.selectMaps(wrapper);
        Object total = rows == null || rows.isEmpty() ? null : getRowValue(rows.get(0), "total");
        return toBigDecimal(total);
    }

    /**
     * 构建单个周期的订单、销售额和增长率。
     *
     * @param currentStart  当前周期开始时间
     * @param currentEnd    当前周期结束时间
     * @param previousStart 上一周期开始时间
     * @param previousEnd   上一周期结束时间
     * @return 周期统计项
     */
    private DashboardPeriodVO.PeriodItem buildPeriodItem(LocalDateTime currentStart, LocalDateTime currentEnd,
                                                         LocalDateTime previousStart, LocalDateTime previousEnd) {
        long currentCount = orderService.count(buildOrderWrapper(currentStart, currentEnd));
        BigDecimal currentSales = querySales(buildSalesWrapper(currentStart, currentEnd));
        long previousCount = orderService.count(buildOrderWrapper(previousStart, previousEnd));
        BigDecimal previousSales = querySales(buildSalesWrapper(previousStart, previousEnd));
        return new DashboardPeriodVO.PeriodItem(
                currentCount,
                currentSales,
                calculateGrowthRate(currentCount - previousCount, previousCount, 2).doubleValue(),
                calculateSalesGrowth(currentSales, previousSales));
    }

    /**
     * 查询每个商品的最小 SKU 库存。
     *
     * @param goodsIds 商品 ID 列表
     * @return 商品 ID 到最小库存的映射
     */
    private Map<Long, Integer> queryMinimumSkuStock(List<Long> goodsIds) {
        if (CollectionUtils.isEmpty(goodsIds)) {
            return Map.of();
        }
        return goodsProductService.list(Wrappers.lambdaQuery(GoodsProduct.class)
                        .in(GoodsProduct::getGoodsId, goodsIds))
                .stream()
                .collect(Collectors.toMap(
                        GoodsProduct::getGoodsId,
                        product -> product.getNumber() == null ? 0 : product.getNumber(),
                        Integer::min));
    }

    /**
     * 按 SQL 排序结果构建商品销量映射。
     * <p>
     * 使用 LinkedHashMap 保留数据库聚合后的热销排序，后续批量查询商品详情时不会丢失排名。
     *
     * @param salesRows 订单明细聚合行
     * @return 商品 ID 到真实销量的有序映射
     */
    private Map<Long, Integer> buildTopGoodsSalesMap(List<Map<String, Object>> salesRows) {
        Map<Long, Integer> salesMap = new LinkedHashMap<>();
        for (Map<String, Object> row : salesRows) {
            Long goodsId = toLongObject(getRowValue(row, "goodsId"));
            if (goodsId != null) {
                salesMap.put(goodsId, toInteger(getRowValue(row, "actualSales"), 0));
            }
        }
        return Collections.unmodifiableMap(salesMap);
    }

    /**
     * 构建热销商品 VO。
     *
     * @param goods       商品实体
     * @param actualSales 真实聚合销量
     * @param stock       商品最小 SKU 库存
     * @return 热销商品展示对象
     */
    private DashboardTopGoodsVO buildTopGoods(Goods goods, Integer actualSales, Integer stock) {
        DashboardTopGoodsVO vo = new DashboardTopGoodsVO();
        vo.setGoodsId(goods.getId());
        vo.setName(goods.getName());
        vo.setPicUrl(goods.getPicUrl());
        vo.setActualSales(actualSales);
        vo.setRetailPrice(goods.getRetailPrice());
        vo.setStock(stock);
        vo.setSku(goods.getGoodsSn());
        return vo;
    }

    /**
     * 计算销售额增长率。
     *
     * @param currentSales  当前销售额
     * @param previousSales 上一周期销售额
     * @return 增长率百分比
     */
    private double calculateSalesGrowth(BigDecimal currentSales, BigDecimal previousSales) {
        if (previousSales.compareTo(BigDecimal.ZERO) == 0) {
            return 0D;
        }
        return currentSales.subtract(previousSales)
                .multiply(BigDecimal.valueOf(100))
                .divide(previousSales, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 计算比例类指标。
     *
     * @param numerator   分子
     * @param denominator 分母
     * @param scale       小数位
     * @return 百分比数值
     */
    private BigDecimal calculateGrowthRate(long numerator, long denominator, int scale) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

    /**
     * 解析支付渠道名称。
     *
     * @param payTypeValue 支付方式字段值
     * @return 渠道名称
     */
    private String resolveChannelName(Object payTypeValue) {
        if (!(payTypeValue instanceof Number number)) {
            return "其他";
        }
        // 兼容历史枚举中 WX_JSAPI 描述错误的问题，看板侧展示真实渠道名。
        if (number.intValue() == PayTypeEnum.WX_JSAPI.getType()) {
            return "微信JSAPI";
        }
        String channelName = PayTypeEnum.getDescByPayType(number.intValue());
        return channelName == null ? "其他" : channelName;
    }

    /**
     * 读取聚合行字段并兼容不同 JDBC 驱动的别名大小写。
     *
     * @param row   聚合行
     * @param field 字段别名
     * @return 字段值
     */
    private Object getRowValue(Map<String, Object> row, String field) {
        if (row == null) {
            return null;
        }
        if (row.containsKey(field)) {
            return row.get(field);
        }
        String lowerField = field.toLowerCase();
        String upperField = field.toUpperCase();
        if (row.containsKey(lowerField)) {
            return row.get(lowerField);
        }
        return row.get(upperField);
    }

    /**
     * 读取聚合行字符串字段。
     *
     * @param row   聚合行
     * @param field 字段别名
     * @return 字符串字段值
     */
    private String getRowString(Map<String, Object> row, String field) {
        Object value = getRowValue(row, field);
        return value == null ? "" : value.toString();
    }

    /**
     * 转换为 long 数值。
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return long 数值
     */
    private long toLong(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 转换为 Long 对象。
     *
     * @param value 原始值
     * @return Long 数值，空值返回 null
     */
    private Long toLongObject(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 转换为 Integer 数值。
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return Integer 数值
     */
    private Integer toInteger(Object value, Integer defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 转换为 BigDecimal。
     *
     * @param value 原始值
     * @return BigDecimal 数值，空值返回 0
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}

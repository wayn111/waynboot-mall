package com.wayn.common.wapper.epay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.wayn.common.config.EpayConfig;
import com.wayn.common.wapper.epay.requet.EpayPrepareRequest;
import com.wayn.common.wapper.epay.requet.EpayRefundRequest;
import com.wayn.common.wapper.epay.response.EpayResponse;
import com.wayn.common.wapper.epay.util.EpaySignUtil;
import com.wayn.common.wapper.epay.util.HttpBuildQuery;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: waynaqua
 * @date: 2024/4/30 14:50
 */
@Slf4j
@Component
public class Epayapi {

    private static final String PREPARE_URL = "mapi.php";
    private static final String SUBMIT_URL = "submit.php";
    private static final String REFUND_URL = "api.php?act=refund";

    @Resource
    private EpayConfig epayConfig;

    /**
     * api下单
     *
     * @return
     * @see <a href="https://epay.beiyunzd.cn/doc.html#pay5">易支付文档</a>
     */
    public String prepare(EpayPrepareRequest request) {
        log.info("epay prepare req is {}", request);
        Map<String, Object> map = BeanUtil.beanToMap(request, false, true);
        String sign = EpaySignUtil.sign(map, epayConfig.getKey());
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        String result = HttpUtil.post(epayConfig.getApiurl() + PREPARE_URL, map);
        log.info("epay prepare result is {}", result);
        // 测试商品¬ify_url
        return result;
    }

    /**
     * 拼接支付链接
     *
     * @return
     * @see <a href="https://epay.beiyunzd.cn/doc.html#pay5">易支付文档</a>
     */
    public String submit(EpayPrepareRequest request) {
        log.info("epay submit req is {}", request);
        Map<String, Object> map = BeanUtil.beanToMap(request, false, true);
        String sign = EpaySignUtil.sign(map, epayConfig.getKey());
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        String query = HttpBuildQuery.httpBuildQuery(map);
        String url = epayConfig.getApiurl() + SUBMIT_URL + "?" + query;
        log.info("epay submit result is {}", url);
        return url;
    }

    /**
     * 订单退款
     *
     * @return
     */
    public String refund(EpayRefundRequest request) {
        log.info("epay refund req is {}", request);
        Map<String, Object> map = BeanUtil.beanToMap(request, false, true);
        String result = HttpUtil.post(epayConfig.getApiurl() + REFUND_URL, map);
        log.info("epay refund result is {}", result);
        EpayResponse epayResponse = JSONObject.parseObject(result, EpayResponse.class);
        if (!epayResponse.isSuccess()) {
            throw new BusinessException(ReturnCodeEnum.CUSTOM_ERROR.setMsg("易支付退款失败"));
        }
        return epayResponse.getMsg();
    }
}

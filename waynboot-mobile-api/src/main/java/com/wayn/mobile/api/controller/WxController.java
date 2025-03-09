package com.wayn.mobile.api.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.wayn.common.response.JsSdkInitResVO;
import com.wayn.common.util.SHA1Util;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 用户接口
 */
@Slf4j
@RestController
@RequestMapping("wx")
public class WxController {

    @Autowired
    private RedisCache redisCache;
    @Value("${wx.appId}")
    private String appId;
    @Value("${wx.appSecret}")
    private String appSecret;

    /**
     * 微信环境js-sdk初始化
     *
     * @param url
     * @return
     * @throws NoSuchAlgorithmException
     */
    @GetMapping("jsSdkInit")
    public R<JsSdkInitResVO> jsSdkInit(String url) throws NoSuchAlgorithmException {
        return R.success(getJsSdkSign(url));
    }

    /**
     * 获取AccessToken
     *
     * @return
     */
    private String getAccessToken() {
        String errMsg = "微信获取accessToken失败";
        String redisKey = "HLMCheckup:HhyWechat:AccessToken";
        String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
        // 去redis里取accessToken
        String accessToken = redisCache.getCacheObject(redisKey);
        if (StringUtils.isEmpty(accessToken)) {
            // redis里的accessToken已过期，重新计算
            String param = "?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            String resultJsonStr = HttpUtil.get(accessTokenUrl + param);
            if (StringUtils.isEmpty(resultJsonStr)) {
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }
            log.info("getAccessToken res is {}", resultJsonStr);
            JSONObject resultJsonObject = JSONObject.parseObject(resultJsonStr);
            Object errcode = resultJsonObject.get("errcode");
            if (errcode != null) {
                log.error("微信获取accessToken失败，errcode:{}，msg:{}", errcode, resultJsonObject.get("errmsg"));
                throw new BusinessException(ReturnCodeEnum.WX_ERROR);
            }
            accessToken = resultJsonObject.getObject("access_token", String.class);
            Long expires = resultJsonObject.getObject("expires_in", Long.class);
            redisCache.setCacheObject(redisKey, accessToken);
            redisCache.expire(redisKey, expires);
        }
        return accessToken;
    }

    private String getTicket() {
        String ticketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
        String ticketRedisKey = "HLMCheckup:HhyWechat:JsApi";
        String ticket = redisCache.getCacheObject(ticketRedisKey);
        if (StringUtils.isNotEmpty(ticket)) {
            return ticket;
        }
        String param = "access_token=" + getAccessToken() + "&type=jsapi";
        String resultJsonStr = HttpUtil.get(ticketUrl + "?" + param);
        if (StringUtils.isEmpty(resultJsonStr)) {
            throw new BusinessException("获取jsapi失败");
        }
        log.info("getTicket res is {}", resultJsonStr);
        JSONObject resultJsonObject = JSONObject.parseObject(resultJsonStr);
        Integer errcode = resultJsonObject.getObject("errcode", Integer.class);
        if (errcode == 0) {
            ticket = resultJsonObject.getObject("ticket", String.class);
            Long expires = resultJsonObject.getObject("expires_in", Long.class);
            redisCache.setCacheObject(ticketRedisKey, ticket);
            redisCache.expire(ticketRedisKey, expires);
        } else {
            log.error("获取jsapi失败:{}", resultJsonObject.getObject("errmsg", String.class));
            throw new BusinessException("获取jsapi失败");
        }
        return ticket;
    }

    /**
     * 获取js-sdk签名
     *
     * @param url 调用方传递的参数
     * @return
     */
    public JsSdkInitResVO getJsSdkSign(String url) throws NoSuchAlgorithmException {
        JsSdkInitResVO resVO = new JsSdkInitResVO();
        Map paramMap = new HashMap();
        paramMap.put("url", url);
        String noncestr = generateRandomString();
        paramMap.put("noncestr", noncestr);
        paramMap.put("jsapi_ticket", getTicket());
        long timestamp = System.currentTimeMillis() / 1000;
        paramMap.put("timestamp", timestamp);
        String requestParam = formatUrlMap(paramMap, false, false);
        String signature = SHA1Util.sha1(requestParam);
        resVO.setAppId(appId);
        resVO.setNonceStr(noncestr);
        resVO.setTimestamp(String.valueOf(timestamp));
        resVO.setSignature(signature);
        return resVO;
    }

    /**
     * 获取16位随机字符串
     *
     * @return
     */
    public String generateRandomString() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(16);
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            sb.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        }
        return sb.toString();
    }

    /**
     * @description: 将参数按照字段名排序
     */
    public static String formatUrlMap(Map<String, Object> paraMap, boolean urlEncode, boolean keyToLower) {
        String buff = "";
        try {
            List<Map.Entry<String, Object>> infoIds = new ArrayList<>(paraMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            infoIds.sort(Map.Entry.comparingByKey());
            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, Object> item : infoIds) {
                if (StringUtils.isNotBlank(item.getKey())) {
                    String key = item.getKey();
                    Object val = item.getValue();
                    if (urlEncode) {
                        val = URLEncoder.encode(val.toString(), StandardCharsets.UTF_8);
                    }
                    if (keyToLower) {
                        buf.append(key.toLowerCase()).append("=").append(val);
                    } else {
                        buf.append(key).append("=").append(val);
                    }
                    buf.append("&");
                }
            }

            buff = buf.toString();
            if (!buff.isEmpty()) {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e) {
            return null;
        }
        return buff;
    }

}

package com.wayn.mobile.api.controller;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Member;
import com.wayn.common.core.entity.shop.ShopCoupon;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.shop.ShopCouponService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.request.GenMobileCodeReqVO;
import com.wayn.common.response.CaptchaResVO;
import com.wayn.common.util.ProfileUtil;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.framework.security.LoginObj;
import com.wayn.mobile.framework.security.service.LoginService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.IdUtil;
import com.wayn.util.util.R;
import com.wf.captcha.SpecCaptcha;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.enumerate.SupplierType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.wayn.data.redis.constant.RedisKeyEnum.MOBILE_CODE_CACHE;
import static com.wayn.data.redis.constant.RedisKeyEnum.MOBILE_CODE_SEND_CACHE;
import static com.wayn.util.enums.ReturnCodeEnum.*;

/**
 * 登录相关接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@Slf4j
@RestController
@AllArgsConstructor
public class LoginController {

    private LoginService loginService;
    private RedisCache redisCache;
    private IMemberService iMemberService;
    private ProfileUtil profileUtil;
    private ShopCouponService shopCouponService;
    private ShopMemberCouponService shopMemberCouponService;

    /**
     * 用户登录
     *
     * @param loginObj 登录参数
     * @return R
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody @Validated LoginObj loginObj) {
        log.info("login req is {}", loginObj.toString());
        String mobile = loginObj.getMobile();
        String yzm = loginObj.getYzm();
        // 校验验证码
        Object value = redisCache.getCacheObject(MOBILE_CODE_CACHE.getKey(mobile + "_" + yzm));
        if (!Objects.equals("1", value)) {
            return R.error(YZM_ENTER_ERROR);
        }
        // 验证手机号是否唯一
        long count = iMemberService.count(Wrappers.lambdaQuery(Member.class).eq(Member::getMobile, mobile));
        if (count > 0) {
            // 生成令牌
            String token = loginService.login(mobile, SysConstants.DEFAULT_PASSWORD);
            redisCache.deleteObject(MOBILE_CODE_CACHE.getKey(mobile + "_" + yzm));
            return R.success(token);
        }
        Member member = new Member();
        long time = System.currentTimeMillis();
        member.setNickname("昵称" + time / 1000);
        String avatar = SysConstants.DEFAULT_AVATAR;
        member.setAvatar(avatar);
        member.setMobile(mobile);
        member.setPassword(MobileSecurityUtils.encryptPassword(SysConstants.DEFAULT_PASSWORD));
        member.setCreateTime(new Date());
        iMemberService.save(member);
        // 赠送注册优惠券
        List<ShopCoupon> list = shopCouponService.lambdaQuery()
                .eq(ShopCoupon::getType, 1)
                .eq(ShopCoupon::getStatus, 1)
                .gt(ShopCoupon::getExpireTime, new Date())
                .list();
        for (ShopCoupon shopCoupon : list) {
            ShopMemberCoupon entity = new ShopMemberCoupon();
            entity.setCouponId(shopCoupon.getId());
            entity.setUserId(Math.toIntExact(member.getId()));
            entity.setMin(shopCoupon.getMin());
            entity.setDiscount(shopCoupon.getDiscount());
            entity.setTitle(shopCoupon.getTitle());
            entity.setUseStatus(0);
            entity.setExpireTime(shopCoupon.getExpireTime());
            entity.setCreateTime(new Date());
            shopMemberCouponService.save(entity);
        }
        // 生成令牌
        String token = loginService.login(mobile, SysConstants.DEFAULT_PASSWORD);
        redisCache.deleteObject(MOBILE_CODE_CACHE.getKey(mobile + "_" + yzm));
        return R.success(token);
    }

    /**
     * 验证码
     *
     * @return R
     */
    @ResponseBody
    @RequestMapping("/captcha")
    public R<CaptchaResVO> captcha() {
        // 创建验证码对象，定义验证码图形的长、宽、以及字数
        SpecCaptcha specCaptcha = new SpecCaptcha(80, 32, 4);
        // 生成验证码
        String verCode = specCaptcha.text().toLowerCase();
        // 生成验证码唯一key
        String key = RedisKeyEnum.CAPTCHA_KEY_CACHE.getKey(IdUtil.getUid());
        // 存入redis并设置过期时间为30分钟
        redisCache.setCacheObject(key, verCode, RedisKeyEnum.CAPTCHA_KEY_CACHE.getExpireSecond());
        // 将key和base64返回给前端
        CaptchaResVO resVO = new CaptchaResVO();
        resVO.setKey(key);
        resVO.setImage(specCaptcha.toBase64());
        return R.success(resVO);
    }

    // 发送短信
    @RequestMapping("genMobileCode")
    public R<Boolean> genMobileCode(@RequestBody @Validated GenMobileCodeReqVO reqVO) {
        log.info("genMobileCode req is {}", reqVO);
        String mobile = reqVO.getMobile();
        if (!PhoneUtil.isMobile(mobile)) {
            throw new BusinessException(MOBILE_ERROR);
        }
        Long ttl = redisCache.ttl(MOBILE_CODE_SEND_CACHE.getKey(mobile));
        if (ttl > 0) {
            throw new BusinessException(YZM_ENTER_BUSY_ERROR);
        }
        String code = "1234";
        if (profileUtil.isProd()) {
            SmsBlend smsBlend = SmsFactory.createSmsBlend(SupplierType.ALIBABA);
            code = RandomUtil.randomNumbers(4);
            SmsResponse smsResponse = smsBlend.sendMessage(mobile, code);
            log.info("smsResponse is {}", smsResponse);
            if (!smsResponse.isSuccess()) {
                redisCache.setCacheObject(MOBILE_CODE_SEND_CACHE.getKey(mobile), "1", MOBILE_CODE_SEND_CACHE.getExpireSecond());
                throw new BusinessException(MOBILE_YZM_SEND_ERROR.getCode(), smsResponse.getMessage());
            }
        }
        log.info("手机号：{}，发送短信：{}", mobile, code);
        redisCache.setCacheObject(MOBILE_CODE_SEND_CACHE.getKey(mobile), "1", MOBILE_CODE_SEND_CACHE.getExpireSecond());
        redisCache.setCacheObject(MOBILE_CODE_CACHE.getKey(mobile + "_" + code), "1", MOBILE_CODE_CACHE.getExpireSecond());
        return R.success();
    }
}

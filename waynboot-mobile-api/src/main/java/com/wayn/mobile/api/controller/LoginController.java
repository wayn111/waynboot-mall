package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Member;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.response.CaptchaResVO;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.framework.security.LoginObj;
import com.wayn.mobile.framework.security.RegistryObj;
import com.wayn.mobile.framework.security.service.LoginService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.util.IdUtil;
import com.wayn.util.util.R;
import com.wf.captcha.SpecCaptcha;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.wayn.util.enums.ReturnCodeEnum.*;

/**
 * 登录相关接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@RestController
@AllArgsConstructor
public class LoginController {

    private LoginService loginService;
    private RedisCache redisCache;
    private IMemberService iMemberService;

    /**
     * 用户登录
     *
     * @param loginObj 登录参数
     * @return R
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody @Validated LoginObj loginObj) {
        // 生成令牌
        String token = loginService.login(loginObj.getMobile(), loginObj.getPassword());
        return R.success(token);
    }

    /**
     * 用户注册
     *
     * @param registryObj 注册参数
     * @return R
     */
    @PostMapping("/registry")
    public R<Boolean> registry(@RequestBody @Validated RegistryObj registryObj) {
        if (!StringUtils.equals(registryObj.getPassword(), registryObj.getConfirmPassword())) {
            return R.error(USER_TWO_PASSWORD_NOT_SAME_ERROR);
        }
        // 验证手机号是否唯一
        long count = iMemberService.count(Wrappers.lambdaQuery(Member.class).eq(Member::getMobile, registryObj.getMobile()));
        if (count > 0) {
            return R.error(USER_PHONE_HAS_REGISTER_ERROR);
        }

        // 判断图形验证码
        String redisCaptchaCode = redisCache.getCacheObject(registryObj.getCaptchaKey());
        if (redisCaptchaCode == null || registryObj.getCaptchaCode() == null || !redisCaptchaCode.equals(registryObj.getCaptchaCode().trim().toLowerCase())) {
            return R.error(USER_CAPTCHA_CODE_ERROR);
        }

        // 删除验证码
        redisCache.deleteObject(registryObj.getCaptchaKey());
        Member member = new Member();
        long time = System.currentTimeMillis();
        member.setNickname("昵称" + time / 1000);
        String avatar = SysConstants.DEFAULT_AVATAR;
        member.setAvatar(avatar);
        member.setMobile(registryObj.getMobile());
        member.setPassword(MobileSecurityUtils.encryptPassword(registryObj.getPassword()));
        member.setCreateTime(new Date());
        return R.result(iMemberService.save(member));
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

}

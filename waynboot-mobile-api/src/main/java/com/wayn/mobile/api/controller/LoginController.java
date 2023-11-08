package com.wayn.mobile.api.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.common.util.mail.MailUtil;
import com.wayn.common.util.security.SecurityUtils;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.framework.security.LoginObj;
import com.wayn.mobile.framework.security.RegistryObj;
import com.wayn.mobile.framework.security.service.LoginService;
import com.wf.captcha.SpecCaptcha;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;

import static com.wayn.common.enums.ReturnCodeEnum.*;

@RestController
@AllArgsConstructor
public class LoginController {

    private LoginService loginService;
    private RedisCache redisCache;
    private IMemberService iMemberService;
    private IMailConfigService mailConfigService;
    private ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    @PostMapping("/login")
    public R login(@RequestBody LoginObj loginObj) {
        // 生成令牌
        String token = loginService.login(loginObj.getMobile(), loginObj.getPassword());
        return R.success().add(SysConstants.TOKEN, token);
    }

    @PostMapping("/registry")
    public R registry(@RequestBody RegistryObj registryObj) {
        if (!StringUtils.equalsIgnoreCase(registryObj.getPassword(), registryObj.getConfirmPassword())) {
            return R.error(USER_TWO_PASSWORD_NOT_SAME_ERROR);
        }
        // 验证手机号是否唯一
        long count = iMemberService.count(Wrappers.lambdaQuery(Member.class).eq(Member::getMobile, registryObj.getMobile()));
        if (count > 0) {
            return R.error(USER_PHONE_HAS_REGISTER_ERROR);
        }

        // 判断图形验证码
        String redisCaptchaCode = redisCache.getCacheObject(registryObj.getCaptchaKey());
        if (registryObj.getCaptchaCode() == null || !redisCaptchaCode.equals(registryObj.getCaptchaCode().trim().toLowerCase())) {
            return R.error(USER_CAPTCHA_CODE_ERROR);
        }

        // 判断邮箱验证码
        String value = redisCache.getCacheObject(registryObj.getEmailKey());
        String[] split = value.split("_");
        if (split.length < 2) {
            return R.error(ReturnCodeEnum.USER_EMAIL_CODE_ERROR);
        }
        String redisEmailCode = split[0];
        String mobile = split[1];
        // 判断发送邮箱验证码的手机号是否与用户当前传入手机号一致
        if (!StringUtils.equalsIgnoreCase(mobile, registryObj.getMobile())) {
            return R.error(ReturnCodeEnum.USER_REGISTER_MOBILE_ERROR);
        }
        // 判断用户输入邮箱验证码是否正确
        if (registryObj.getEmailCode() == null || !redisEmailCode.equals(registryObj.getEmailCode().trim().toLowerCase())) {
            return R.error(ReturnCodeEnum.USER_EMAIL_CODE_ERROR);
        }
        // 删除验证码
        redisCache.deleteObject(registryObj.getCaptchaKey());
        redisCache.deleteObject(registryObj.getEmailKey());
        Member member = new Member();
        long time = System.currentTimeMillis();
        member.setNickname("昵称" + time / 1000);
        String avatar = SysConstants.DEFAULT_AVATAR;
        member.setAvatar(avatar);
        member.setMobile(registryObj.getMobile());
        member.setEmail(registryObj.getEmail());
        member.setPassword(SecurityUtils.encryptPassword(registryObj.getPassword()));
        member.setCreateTime(new Date());
        return R.result(iMemberService.save(member));
    }

    @ResponseBody
    @RequestMapping("/captcha")
    public R captcha() {
        // 创建验证码对象，定义验证码图形的长、宽、以及字数
        SpecCaptcha specCaptcha = new SpecCaptcha(80, 32, 4);
        // 生成验证码
        String verCode = specCaptcha.text().toLowerCase();
        // 生成验证码唯一key
        String key = RedisKeyEnum.CAPTCHA_KEY_CACHE.getKey(IdUtil.getUid());
        // 存入redis并设置过期时间为30分钟
        redisCache.setCacheObject(key, verCode, RedisKeyEnum.CAPTCHA_KEY_CACHE.getExpireSecond());
        // 将key和base64返回给前端
        return R.success().add("captchaKey", key).add("captchaImg", specCaptcha.toBase64());
    }

    @PostMapping("/sendEmailCode")
    public R sendEmailCode(@RequestBody RegistryObj registryObj) {
        // 判断图形验证码是否正确
        String captchaKey = registryObj.getCaptchaKey();
        String captchaCode = registryObj.getCaptchaCode();
        String mobile = registryObj.getMobile();
        if (StringUtils.isBlank(captchaKey)) {
            return R.error(CUSTOM_ERROR.setMsg("图形验证码错误"));
        }
        if (StringUtils.isBlank(captchaCode)) {
            return R.error(CUSTOM_ERROR.setMsg("图形验证码为空"));
        }
        if (StringUtils.isBlank(mobile)) {
            return R.error(CUSTOM_ERROR.setMsg("手机号为空"));
        }
        String redisCode = redisCache.getCacheObject(captchaKey);
        // 判断验证码code
        if (!redisCode.equals(captchaCode.trim().toLowerCase())) {
            return R.error(USER_CAPTCHA_CODE_ERROR);
        }
        // 验证手机号是否唯一
        long count = iMemberService.count(Wrappers.lambdaQuery(Member.class).eq(Member::getMobile, mobile));
        if (count > 0) {
            return R.error(USER_PHONE_HAS_REGISTER_ERROR);
        }
        // 生成邮箱验证码code
        String verCode = RandomUtil.randomString(6);
        // 生成邮箱验证码唯一key
        String key = RedisKeyEnum.EMAIL_KEY_CACHE.getKey(IdUtil.getUid());
        // 存入redis并设置过期时间为20分钟
        redisCache.setCacheObject(key, verCode + "_" + mobile,  RedisKeyEnum.EMAIL_KEY_CACHE.getExpireSecond());
        commonThreadPoolTaskExecutor.execute(() -> {
            EmailConfig emailConfig = mailConfigService.getById(1L);
            SendMailVO sendMailVO = new SendMailVO();
            sendMailVO.setSubject("mall商城注册通知");
            sendMailVO.setContent("邮箱验证码：" + verCode);
            sendMailVO.setTos(Collections.singletonList(registryObj.getEmail()));
            MailUtil.sendMail(emailConfig, sendMailVO, false, false);
        });
        return R.success().add("emailKey", key);
    }
}

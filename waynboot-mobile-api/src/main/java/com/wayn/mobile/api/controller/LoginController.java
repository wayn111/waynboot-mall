package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.common.util.mail.MailUtil;
import com.wayn.common.util.security.SecurityUtils;
import com.wayn.mobile.framework.redis.RedisCache;
import com.wayn.mobile.framework.security.LoginObj;
import com.wayn.mobile.framework.security.RegistryObj;
import com.wayn.mobile.framework.security.service.LoginService;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMemberService iMemberService;

    @Autowired
    private IMailConfigService mailConfigService;

    @PostMapping("/login")
    public R login(@RequestBody LoginObj loginObj) {
        String token = loginService.login(loginObj.getMobile(), loginObj.getPassword());
        // 生成令牌
        return R.success().add(SysConstants.TOKEN, token);
    }

    @PostMapping("/registry")
    public R registry(@RequestBody RegistryObj registryObj) {
        if (!StringUtils.equalsIgnoreCase(registryObj.getPassword(), registryObj.getConfirmPassword())) {
            return R.error("两次密码输入不相符");
        }
        // 验证手机号是否唯一
        int count = iMemberService.count(new QueryWrapper<Member>().eq("mobile", registryObj.getMobile()));
        iMemberService.count(new QueryWrapper<Member>().eq("mobile", registryObj.getMobile()));
        if (count > 0) {
            return R.error("手机号已注册，请更换手机号");
        }

        // 获取redis中的验证码
        String redisCode = redisCache.getCacheObject(registryObj.getKey());
        String redisEmailCode = redisCache.getCacheObject(registryObj.getEmailKey());

        // 判断验证码
        if (registryObj.getCode() == null || !redisCode.equals(registryObj.getCode().trim().toLowerCase())) {
            return R.error("验证码不正确");
        }
        // 判断邮箱验证码
        if (registryObj.getEmailCode() == null || !redisEmailCode.equals(registryObj.getEmailCode().trim().toLowerCase())) {
            return R.error("邮箱验证码不正确");
        }
        // 删除验证码
        redisCache.deleteObject(registryObj.getKey());
        redisCache.deleteObject(registryObj.getEmailKey());
        Member member = new Member();
        member.setUsername("新用户" + new Date().getTime());
        member.setNickname("用户昵称" + new Date().getTime());
        member.setMobile(registryObj.getMobile());
        member.setEmail(registryObj.getEmail());
        member.setPassword(SecurityUtils.encryptPassword(registryObj.getPassword()));
        member.setCreateTime(new Date());
        return R.result(iMemberService.save(member));
    }

    @ResponseBody
    @RequestMapping("/captcha")
    public R captcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(80, 32, 4);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.getUid();
        // 存入redis并设置过期时间为30分钟
        redisCache.setCacheObject(key, verCode, SysConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 将key和base64返回给前端
        return R.success().add("key", key).add("image", specCaptcha.toBase64());
    }

    @PostMapping("/sendEmailCode")
    public R sendEmailCode(@RequestBody RegistryObj registryObj) {
        SpecCaptcha specCaptcha = new SpecCaptcha(80, 32, 4);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.getUid();
        // 存入redis并设置过期时间为20分钟
        redisCache.setCacheObject(key, verCode, SysConstants.CAPTCHA_EXPIRATION * 10, TimeUnit.MINUTES);
        EmailConfig emailConfig = mailConfigService.getById(1L);
        SendMailVO sendMailVO = new SendMailVO();
        sendMailVO.setSubject("mall商城注册通知");
        sendMailVO.setContent("邮箱验证码：" + verCode);
        sendMailVO.setTos(Arrays.asList(registryObj.getEmail()));
        MailUtil.sendMail(emailConfig, sendMailVO, false);
        return R.success().add("key", key);
    }
}

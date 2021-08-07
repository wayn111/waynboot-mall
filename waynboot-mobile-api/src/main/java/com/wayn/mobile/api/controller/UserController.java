package com.wayn.mobile.api.controller;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.ProfileVO;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.common.util.mail.MailUtil;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.RegistryObj;
import com.wayn.mobile.framework.security.service.TokenService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private IMemberService iMemberService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMailConfigService mailConfigService;

    @GetMapping("info")
    public R getInfo() {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        return R.success().add("info", loginUser.getMember());
    }

    @PostMapping("profile")
    public R profile(@RequestBody ProfileVO profileVO) {
        String nickname = profileVO.getNickname();
        Integer gender = profileVO.getGender();
        String mobile = profileVO.getMobile();
        String email = profileVO.getEmail();
        LocalDate birthday = profileVO.getBirthday();
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Member member = loginUser.getMember();
        if (StringUtils.isNotBlank(nickname)) {
            member.setNickname(nickname);
        }
        if (Objects.nonNull(gender)) {
            member.setGender(gender);
        }
        if (StringUtils.isNotBlank(mobile)) {
            member.setMobile(mobile);
        }
        if (StringUtils.isNotBlank(email)) {
            member.setEmail(email);
        }
        if (Objects.nonNull(birthday)) {
            member.setBirthday(birthday);
        }
        loginUser.setMember(member);
        tokenService.refreshToken(loginUser);
        return R.result(iMemberService.updateById(member));
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
        sendMailVO.setSubject("mall商城重置密码通知");
        sendMailVO.setContent("邮箱验证码：" + verCode);
        sendMailVO.setTos(Collections.singletonList(registryObj.getEmail()));
        MailUtil.sendMail(emailConfig, sendMailVO, false);
        return R.success().add("key", key);
    }

    @PostMapping("uploadAvatar")
    public R uploadAvatar(String avatar) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Member member = loginUser.getMember();
        member.setAvatar(avatar);
        boolean update = iMemberService.updateById(member);
        if (!update) {
            throw new BusinessException("上传头像失败");
        }
        loginUser.setMember(member);
        tokenService.refreshToken(loginUser);
        return R.result(true).add("userInfo", member);
    }

    @PostMapping("updatePassword")
    public R updatePassword(@RequestBody RegistryObj registryObj) {
        if (!StringUtils.equalsIgnoreCase(registryObj.getPassword(), registryObj.getConfirmPassword())) {
            return R.error(ReturnCodeEnum.USER_TWO_PASSWORD_NOT_SAME_ERROR);
        }
        String redisEmailCode = redisCache.getCacheObject(registryObj.getEmailKey());
        // 判断邮箱验证码
        if (registryObj.getEmailCode() == null || !redisEmailCode.equals(registryObj.getEmailCode().trim().toLowerCase())) {
            return R.error(ReturnCodeEnum.USER_EMAIL_CODE_ERROR);
        }
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Member member = loginUser.getMember();
        member.setPassword(MobileSecurityUtils.encryptPassword(registryObj.getPassword()));
        boolean update = iMemberService.updateById(member);
        if (!update) {
            throw new BusinessException("修改密码失败");
        }
        loginUser.setMember(member);
        tokenService.refreshToken(loginUser);
        return R.result(true).add("userInfo", member);
    }
}

package com.wayn.mobile.api.controller;

import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.vo.ProfileVO;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.service.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private IMemberService iMemberService;

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

    @PostMapping("uploadAvatar")
    public R uploadAvatar(String avatar) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Member member = loginUser.getMember();
        member.setAvatar(avatar);
        boolean update = iMemberService.updateById(member);
        if (!update) {
            throw new BusinessException("");
        }
        loginUser.setMember(member);
        tokenService.refreshToken(loginUser);
        return R.result(update).add("userInfo", member);
    }
}

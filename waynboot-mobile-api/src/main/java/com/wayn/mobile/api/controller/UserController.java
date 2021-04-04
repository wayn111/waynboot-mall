package com.wayn.mobile.api.controller;

import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

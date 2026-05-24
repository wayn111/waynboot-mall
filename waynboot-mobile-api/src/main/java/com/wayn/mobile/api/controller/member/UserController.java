package com.wayn.mobile.api.controller.member;

import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.service.IMemberService;
import com.wayn.common.model.request.ProfileRequestVO;
import com.wayn.common.model.request.UpdatePasswordReqVO;
import com.wayn.common.model.response.MobileUserAvatarResVO;
import com.wayn.common.model.response.MobileUserInfoResVO;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.service.TokenService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.R;
import com.wayn.util.util.ServletUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("user")
public class UserController {

    private final TokenService tokenService;

    private final IMemberService iMemberService;

    /**
     * 获取用户信息
     *
     * @return R
     */
    @GetMapping("info")
    public R<MobileUserInfoResVO> getInfo() {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getMember().getId();
        log.info("获取用户信息开始, userId={}", userId);
        MobileUserInfoResVO resVO = toMobileUserInfoResVO(loginUser.getMember());
        log.info("获取用户信息完成, userId={}", userId);
        return R.success(resVO);
    }

    /**
     * 修改用户资料
     *
     * @param profileRequestVO 用户资料参数
     * @return R
     */
    @PostMapping("profile")
    public R<Boolean> updateProfile(@RequestBody ProfileRequestVO profileRequestVO) {
        String nickname = profileRequestVO.getNickname();
        Integer gender = profileRequestVO.getGender();
        String mobile = profileRequestVO.getMobile();
        String email = profileRequestVO.getEmail();
        LocalDate birthday = profileRequestVO.getBirthday();
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Member member = loginUser.getMember();
        log.info("更新用户资料开始, userId={}, nicknameUpdated={}, genderUpdated={}, mobileUpdated={}, emailUpdated={}, birthdayUpdated={}",
                member.getId(),
                StringUtils.isNotBlank(nickname),
                Objects.nonNull(gender),
                StringUtils.isNotBlank(mobile),
                StringUtils.isNotBlank(email),
                Objects.nonNull(birthday));
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
        boolean updated = iMemberService.updateById(member);
        if (updated) {
            loginUser.setMember(member);
            tokenService.refreshToken(loginUser);
        }
        log.info("更新用户资料完成, userId={}, result={}", member.getId(), updated);
        return R.result(updated);
    }

    /**
     * 上传头像
     *
     * @param avatar 用户头像地址
     * @return R
     */
    @PostMapping("uploadAvatar")
    public R<MobileUserAvatarResVO> uploadAvatar(@RequestParam String avatar) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Member member = loginUser.getMember();
        log.info("上传用户头像开始, userId={}, avatarLength={}", member.getId(), StringUtils.length(avatar));
        member.setAvatar(avatar);
        boolean update = iMemberService.updateById(member);
        if (!update) {
            throw new BusinessException("上传头像失败");
        }
        loginUser.setMember(member);
        tokenService.refreshToken(loginUser);
        log.info("上传用户头像完成, userId={}", member.getId());
        return R.success(toMobileUserAvatarResVO(member));
    }

    /**
     * 更新用户密码
     *
     * @param reqVO 更新参数
     * @return R
     */
    @PostMapping("updatePassword")
    public R<Boolean> updatePassword(@RequestBody @Validated UpdatePasswordReqVO reqVO) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getMember().getId();
        log.info("更新用户密码开始, userId={}", userId);
        String oldPassword = reqVO.getOldPassword();
        if (!MobileSecurityUtils.matchesPassword(oldPassword, loginUser.getPassword())) {
            log.warn("更新用户密码失败, userId={}, reason=old_password_not_match", userId);
            return R.error(ReturnCodeEnum.OLD_PASSWORD_NOT_EQUALS_ERROR);
        }
        if (!StringUtils.equalsIgnoreCase(reqVO.getPassword(), reqVO.getConfirmPassword())) {
            log.warn("更新用户密码失败, userId={}, reason=password_not_same", userId);
            return R.error(ReturnCodeEnum.USER_TWO_PASSWORD_NOT_SAME_ERROR);
        }
        Member member = loginUser.getMember();
        member.setPassword(MobileSecurityUtils.encryptPassword(reqVO.getPassword()));
        boolean update = iMemberService.updateById(member);
        if (!update) {
            throw new BusinessException("修改密码失败");
        }
        loginUser.setMember(member);
        tokenService.refreshToken(loginUser);
        log.info("更新用户密码完成, userId={}", userId);
        return R.success(Boolean.TRUE);
    }

    private MobileUserInfoResVO toMobileUserInfoResVO(Member member) {
        MobileUserInfoResVO resVO = new MobileUserInfoResVO();
        resVO.setId(member.getId());
        resVO.setGender(member.getGender());
        resVO.setBirthday(member.getBirthday());
        resVO.setEmail(member.getEmail());
        resVO.setLastLoginTime(member.getLastLoginTime());
        resVO.setLastLoginIp(member.getLastLoginIp());
        resVO.setUserLevel(member.getUserLevel());
        resVO.setNickname(member.getNickname());
        resVO.setMobile(member.getMobile());
        resVO.setAvatar(member.getAvatar());
        resVO.setStatus(member.getStatus());
        resVO.setCreateTime(member.getCreateTime());
        resVO.setUpdateTime(member.getUpdateTime());
        return resVO;
    }

    private MobileUserAvatarResVO toMobileUserAvatarResVO(Member member) {
        MobileUserAvatarResVO resVO = new MobileUserAvatarResVO();
        resVO.setUserId(member.getId());
        resVO.setAvatar(member.getAvatar());
        return resVO;
    }
}

package com.wayn.mobile.framework.security.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.service.IMemberService;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.enums.UserStatusEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.ip.IpUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 移动端登录编排服务。
 * <p>
 * 该服务使用 Sa-Token 创建登录态，负责用户校验、密码校验、登录会话写入和最后登录信息异步更新。
 */
@Slf4j
@Component
@AllArgsConstructor
public class LoginService {

    private final IMemberService iMemberService;

    private final ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    /**
     * 执行移动端手机号登录，成功后返回 Sa-Token 原始 token 值。
     *
     * @param mobile   手机号
     * @param password 明文密码
     * @return Sa-Token token 值
     */
    public String login(String mobile, String password) {
        Member member = findMemberByMobile(mobile);
        validateMemberStatus(member);
        validatePassword(password, member.getPassword());

        StpUtil.login(member.getId());
        String tokenValue = StpUtil.getTokenValue();
        writeLoginSession(member, tokenValue);
        refreshLastLoginAsync(member.getId());
        log.info("移动端登录成功, userId={}", member.getId());
        return tokenValue;
    }

    /**
     * 根据手机号查询会员，未查询到时抛出统一用户不存在异常。
     *
     * @param mobile 手机号
     * @return 会员信息
     */
    private Member findMemberByMobile(String mobile) {
        Member member = iMemberService.getOne(new QueryWrapper<Member>().eq("mobile", mobile));
        if (member == null) {
            throw new BusinessException(ReturnCodeEnum.USER_NOT_EXISTS_ERROR);
        }
        return member;
    }

    /**
     * 校验会员状态，禁用或注销用户不允许继续登录。
     *
     * @param member 会员信息
     */
    private void validateMemberStatus(Member member) {
        if (UserStatusEnum.DISABLE.getCode().equals(member.getStatus())
                || UserStatusEnum.DELETED.getCode().equals(member.getStatus())) {
            throw new BusinessException(ReturnCodeEnum.FORBIDDEN);
        }
    }

    /**
     * 校验登录密码，失败时返回和原 Spring Security 链路一致的账号密码错误。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 密文密码
     */
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (StringUtils.isBlank(encodedPassword)
                || !MobileSecurityUtils.matchesPassword(rawPassword, encodedPassword)) {
            throw new BusinessException(ReturnCodeEnum.USER_ACCOUNT_PASSWORD_ERROR);
        }
    }

    /**
     * 写入 Sa-Token session 中的登录用户快照，供 Controller 和领域入口读取当前用户。
     *
     * @param member     会员信息
     * @param tokenValue Sa-Token token 值
     */
    private void writeLoginSession(Member member, String tokenValue) {
        long loginTime = System.currentTimeMillis();
        long tokenTimeout = StpUtil.getTokenTimeout();
        long expireTime = tokenTimeout > 0 ? loginTime + TimeUnit.SECONDS.toMillis(tokenTimeout) : -1L;
        LoginUserDetail loginUser = new LoginUserDetail(member, Collections.emptySet())
                .setToken(tokenValue)
                .setLoginTime(loginTime)
                .setExpireTime(expireTime);
        MobileSecurityUtils.refreshLoginUser(loginUser);
    }

    /**
     * 异步记录最后登录时间和登录 IP，避免非核心写入影响登录主链路。
     *
     * @param memberId 会员 ID
     */
    private void refreshLastLoginAsync(Long memberId) {
        try {
            commonThreadPoolTaskExecutor.execute(() -> {
                try {
                    iMemberService.update()
                            .set("last_login_time", LocalDateTime.now())
                            .set("last_login_ip", IpUtils.getHostIp())
                            .eq("id", memberId)
                            .update();
                } catch (Exception e) {
                    log.warn("记录移动端最后登录信息失败, userId={}", memberId, e);
                }
            });
        } catch (Exception e) {
            log.warn("提交移动端最后登录信息任务失败, userId={}", memberId, e);
        }
    }
}

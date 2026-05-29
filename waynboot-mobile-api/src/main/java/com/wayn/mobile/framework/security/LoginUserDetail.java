package com.wayn.mobile.framework.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayn.domain.api.trade.entity.Member;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * 移动端登录用户快照。
 * <p>
 * Sa-Token 只负责 token 与 session 生命周期，本类用于保存业务侧需要的会员信息和权限快照。
 */
@Getter
public class LoginUserDetail implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    private String token;

    private Member member;

    /**
     * 登陆时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 权限列表
     */
    private Set<String> permissions = Collections.emptySet();


    /**
     * 创建仅包含会员信息的登录快照。
     *
     * @param member 会员信息
     */
    public LoginUserDetail(Member member) {
        this.member = member;
    }

    /**
     * 创建包含会员和权限信息的登录快照。
     *
     * @param member      会员信息
     * @param permissions 权限集合
     */
    public LoginUserDetail(Member member, Set<String> permissions) {
        this.member = member;
        this.permissions = permissions == null ? Collections.emptySet() : permissions;
    }

    /**
     * Jackson 反序列化使用的无参构造器。
     */
    public LoginUserDetail() {
    }

    /**
     * 设置权限集合，返回当前对象用于链式赋值。
     *
     * @param permissions 权限集合
     * @return 当前登录快照
     */
    public LoginUserDetail setPermissions(Set<String> permissions) {
        this.permissions = permissions == null ? Collections.emptySet() : permissions;
        return this;
    }

    /**
     * 设置登录时间戳，返回当前对象用于链式赋值。
     *
     * @param loginTime 登录时间戳
     * @return 当前登录快照
     */
    public LoginUserDetail setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
        return this;
    }

    /**
     * 设置过期时间戳，返回当前对象用于链式赋值。
     *
     * @param expireTime 过期时间戳
     * @return 当前登录快照
     */
    public LoginUserDetail setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    /**
     * 设置 token 值，返回当前对象用于链式赋值。
     *
     * @param token token 值
     * @return 当前登录快照
     */
    public LoginUserDetail setToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * 设置会员信息，返回当前对象用于链式赋值。
     *
     * @param member 会员信息
     * @return 当前登录快照
     */
    public LoginUserDetail setMember(Member member) {
        this.member = member;
        return this;
    }

    /**
     * 获取会员密码，仅用于内部密码校验，不参与 JSON 输出。
     *
     * @return 会员密码
     */
    @JsonIgnore
    public String getPassword() {
        return this.member == null ? null : this.member.getPassword();
    }

    /**
     * 获取登录用户名，移动端以手机号作为用户名。
     *
     * @return 手机号
     */
    public String getUsername() {
        return this.member == null ? null : this.member.getMobile();
    }
}

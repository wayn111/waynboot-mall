package com.wayn.mobile.framework.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayn.common.core.domain.shop.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class LoginUserDetail implements UserDetails {


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
    private Set<String> permissions;


    public LoginUserDetail(Member member) {
        this.member = member;
    }

    public LoginUserDetail(Member member, Set<String> permissions) {
        this.member = member;
        this.permissions = permissions;
    }

    public LoginUserDetail() {
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public LoginUserDetail setPermissions(Set<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public LoginUserDetail setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
        return this;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public LoginUserDetail setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public String getToken() {
        return token;
    }

    public LoginUserDetail setToken(String token) {
        this.token = token;
        return this;
    }

    public Member getMember() {
        return member;
    }

    public LoginUserDetail setMember(Member member) {
        this.member = member;
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.member.getPassword();
    }

    @Override
    public String getUsername() {
        return this.member.getMobile();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}

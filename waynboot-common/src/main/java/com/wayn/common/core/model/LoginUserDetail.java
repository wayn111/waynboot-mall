package com.wayn.common.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayn.common.core.domain.system.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class LoginUserDetail implements UserDetails {


    private static final long serialVersionUID = 1L;

    private String token;

    private User user;

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


    public LoginUserDetail(User user) {
        this.user = user;
    }

    public LoginUserDetail(User user, Set<String> permissions) {
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public LoginUserDetail setUser(User user) {
        this.user = user;
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUserName();
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

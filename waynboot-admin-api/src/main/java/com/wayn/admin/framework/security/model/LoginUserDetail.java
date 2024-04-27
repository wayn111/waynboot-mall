package com.wayn.admin.framework.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayn.common.core.entity.system.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;

@Getter
public class LoginUserDetail implements UserDetails {


    @Serial
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

    public LoginUserDetail setPermissions(Set<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public LoginUserDetail setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
        return this;
    }

    public LoginUserDetail setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public LoginUserDetail setToken(String token) {
        this.token = token;
        return this;
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

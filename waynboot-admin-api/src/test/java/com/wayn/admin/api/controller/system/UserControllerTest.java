package com.wayn.admin.api.controller.system;

import com.wayn.admin.framework.security.service.TokenService;
import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.common.core.entity.system.User;
import com.wayn.common.core.service.system.IRoleService;
import com.wayn.common.core.service.system.IUserService;
import com.wayn.util.enums.UserStatusEnum;
import com.wayn.util.util.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;
    @Mock
    private IRoleService roleService;
    @Mock
    private TokenService tokenService;

    @Test
    void deleteUserShouldClearTokensAfterDatabaseRemoval() {
        UserController controller = new UserController(userService, roleService, tokenService);
        List<Long> userIds = List.of(101L, 102L);
        when(userService.removeByIds(userIds)).thenReturn(true);

        R<Boolean> result = controller.deleteUser(userIds);

        assertThat(result.getCode()).isEqualTo(200);
        verify(tokenService).clearUserTokens(userIds);
    }

    @Test
    void deleteUserShouldClearTokensForAlreadyDeletedUsersWhenDatabaseRemovalFailed() {
        UserController controller = new UserController(userService, roleService, tokenService);
        List<Long> userIds = List.of(201L);
        when(userService.removeByIds(userIds)).thenReturn(false);
        when(userService.getById(201L)).thenReturn(null);

        R<Boolean> result = controller.deleteUser(userIds);

        assertThat(result.getCode()).isEqualTo(500);
        verify(tokenService).clearUserTokens(userIds);
    }

    @Test
    void deleteUserShouldNotClearTokensWhenDatabaseRemovalFailedAndUserStillExists() {
        UserController controller = new UserController(userService, roleService, tokenService);
        List<Long> userIds = List.of(202L);
        User user = new User();
        user.setUserId(202L);
        when(userService.removeByIds(userIds)).thenReturn(false);
        when(userService.getById(202L)).thenReturn(user);

        R<Boolean> result = controller.deleteUser(userIds);

        assertThat(result.getCode()).isEqualTo(500);
        verify(tokenService, never()).clearUserTokens(userIds);
    }

    @Test
    void changeStatusShouldClearTokensWhenUserIsDisabled() {
        UserController controller = new UserController(userService, roleService, tokenService);
        User user = new User();
        user.setUserId(301L);
        user.setUserStatus(UserStatusEnum.DISABLE.getCode());
        when(userService.updateById(user)).thenReturn(true);
        mockLoginContext("admin");

        R<Boolean> result = controller.changeStatus(user);

        assertThat(result.getCode()).isEqualTo(200);
        verify(tokenService).clearUserTokens(301L);
        SecurityContextHolder.clearContext();
    }

    @Test
    void changeStatusShouldNotClearTokensWhenUserIsEnabled() {
        UserController controller = new UserController(userService, roleService, tokenService);
        User user = new User();
        user.setUserId(401L);
        user.setUserStatus(UserStatusEnum.OK.getCode());
        when(userService.updateById(user)).thenReturn(true);
        mockLoginContext("admin");

        R<Boolean> result = controller.changeStatus(user);

        assertThat(result.getCode()).isEqualTo(200);
        verify(tokenService, never()).clearUserTokens(401L);
        SecurityContextHolder.clearContext();
    }

    private void mockLoginContext(String username) {
        User loginUser = new User();
        loginUser.setUserId(1L);
        loginUser.setUserName(username);
        LoginUserDetail loginUserDetail = new LoginUserDetail(loginUser);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserDetail, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}

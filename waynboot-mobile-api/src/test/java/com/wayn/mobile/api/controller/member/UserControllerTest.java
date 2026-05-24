package com.wayn.mobile.api.controller.member;

import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.service.IMemberService;
import com.wayn.common.model.request.UpdatePasswordReqVO;
import com.wayn.common.model.response.MobileUserAvatarResVO;
import com.wayn.common.model.response.MobileUserInfoResVO;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.service.TokenService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private TokenService tokenService;
    @Mock
    private IMemberService memberService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getInfoShouldReturnMobileUserInfoVo() {
        UserController controller = new UserController(tokenService, memberService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Member member = buildMember();
        LoginUserDetail loginUser = new LoginUserDetail(member);
        when(tokenService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);

        R<MobileUserInfoResVO> result = controller.getInfo();

        assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(member.getId(), result.getData().getId());
        assertEquals(member.getNickname(), result.getData().getNickname());
        assertEquals(member.getMobile(), result.getData().getMobile());
        assertEquals(member.getAvatar(), result.getData().getAvatar());
    }

    @Test
    void uploadAvatarShouldReturnAvatarVo() {
        UserController controller = new UserController(tokenService, memberService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Member member = buildMember();
        LoginUserDetail loginUser = new LoginUserDetail(member);
        when(tokenService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(memberService.updateById(any(Member.class))).thenReturn(true);

        R<MobileUserAvatarResVO> result = controller.uploadAvatar("https://img.example.com/avatar.png");

        assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(member.getId(), result.getData().getUserId());
        assertEquals("https://img.example.com/avatar.png", result.getData().getAvatar());
        verify(tokenService).refreshToken(loginUser);
    }

    @Test
    void updatePasswordShouldReturnBooleanResult() {
        UserController controller = new UserController(tokenService, memberService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Member member = buildMember();
        member.setPassword("old-encoded");
        LoginUserDetail loginUser = new LoginUserDetail(member);
        when(tokenService.getLoginUser(any(HttpServletRequest.class))).thenReturn(loginUser);
        when(memberService.updateById(any(Member.class))).thenReturn(true);
        UpdatePasswordReqVO reqVO = new UpdatePasswordReqVO();
        reqVO.setOldPassword("old");
        reqVO.setPassword("new-password");
        reqVO.setConfirmPassword("new-password");

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(() -> MobileSecurityUtils.matchesPassword("old", "old-encoded")).thenReturn(true);
            mockedSecurity.when(() -> MobileSecurityUtils.encryptPassword("new-password")).thenReturn("new-encoded");

            R<Boolean> result = controller.updatePassword(reqVO);

            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberService).updateById(memberCaptor.capture());
            assertEquals("new-encoded", memberCaptor.getValue().getPassword());
            verify(tokenService).refreshToken(loginUser);
        }
    }

    private Member buildMember() {
        Member member = new Member();
        member.setId(7L);
        member.setNickname("wayn");
        member.setMobile("13800138000");
        member.setAvatar("https://img.example.com/avatar-old.png");
        member.setEmail("wayn@example.com");
        member.setGender(1);
        member.setBirthday(LocalDate.of(2020, 1, 1));
        member.setStatus(0);
        member.setUserLevel(0);
        return member;
    }
}

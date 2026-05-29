package com.wayn.mobile.framework.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobileAuthorizationHeaderFilterTest {

    @Test
    void shouldKeepRawAuthorizationToken() throws ServletException, IOException {
        MobileAuthorizationHeaderFilter filter = new MobileAuthorizationHeaderFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "e12111b6-2188-4c6c-9ce4-aefd1ded1917";
        request.addHeader("Authorization", token);

        FilterChain chain = (servletRequest, servletResponse) ->
                assertEquals(token, ((HttpServletRequest) servletRequest).getHeader("Authorization"));

        filter.doFilter(request, response, chain);
    }

    @Test
    void shouldStripBearerPrefixFromAuthorizationToken() throws ServletException, IOException {
        MobileAuthorizationHeaderFilter filter = new MobileAuthorizationHeaderFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "e12111b6-2188-4c6c-9ce4-aefd1ded1917";
        request.addHeader("Authorization", "Bearer " + token);

        FilterChain chain = (servletRequest, servletResponse) ->
                assertEquals(token, ((HttpServletRequest) servletRequest).getHeader("Authorization"));

        filter.doFilter(request, response, chain);
    }
}

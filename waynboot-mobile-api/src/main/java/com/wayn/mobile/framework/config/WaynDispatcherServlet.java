package com.wayn.mobile.framework.config;

import com.wayn.common.base.entity.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 自定义DispatcherServlet，记录请求响应日志
 */
@Slf4j
public class WaynDispatcherServlet extends DispatcherServlet {


    private static final long serialVersionUID = -3147711009552694708L;

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        HandlerExecutionChain handler = getHandler(request);
        long start = System.currentTimeMillis();
        try {
            super.doDispatch(request, response);
        } finally {
            log(request, response, handler, start);
            updateResponse(response);
        }
    }

    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache, HandlerExecutionChain handler, long start) {
        long end = System.currentTimeMillis();
        LogMessage log = new LogMessage();
        log.setHttpStatus(responseToCache.getStatus());
        log.setHttpMethod(requestToCache.getMethod());
        Map<String, String[]> parameterMap = requestToCache.getParameterMap();
        StringBuilder parameter = new StringBuilder("[");
        int size = parameterMap.size();
        int count = 0;
        for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
            count++;
            parameter.append(stringEntry.getKey());
            parameter.append(":");
            parameter.append(stringEntry.getValue()[0]);
            if (count < size) {
                parameter.append(",");
            } else {
                parameter.append("]");
            }
        }
        log.setReqParameter(parameter.toString());
        log.setPath(requestToCache.getRequestURI());
        log.setClientIp(requestToCache.getRemoteAddr());
        log.setJavaMethod(handler.toString());
        log.setExecutionTime(end - start);
        log.setResponse(getResponsePayload(responseToCache));
        logger.info(log);
    }

    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {

            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);
                return new String(buf, 0, length, StandardCharsets.UTF_8);
            }
        }
        return "[unknown]";
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        responseWrapper.copyBodyToResponse();
    }

}

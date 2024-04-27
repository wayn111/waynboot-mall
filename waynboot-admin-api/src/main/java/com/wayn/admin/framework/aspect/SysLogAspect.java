package com.wayn.admin.framework.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.annotation.Log;
import com.wayn.common.core.entity.system.SysLog;
import com.wayn.common.core.service.system.ISysLogService;
import com.wayn.util.util.ServletUtils;
import com.wayn.util.util.http.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Aspect
@Component
public class SysLogAspect {

    private static final ThreadLocal<Long> startTimeLocal = ThreadLocal.withInitial(() -> 0L);

    @Autowired
    private ISysLogService iSysLogService;

    @Pointcut("@annotation(com.wayn.common.annotation.Log)")
    public void logPointCut() {
    }

    /**
     * 当方法执行前
     *
     * @param joinPoint 切点
     */
    @Before(value = "logPointCut()")
    public void doBefore(JoinPoint joinPoint) {
        startTimeLocal.set(System.nanoTime());
    }

    /**
     * 当方法正常返回时执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(value = "logPointCut()", returning = "response")
    public void doAfterReturning(JoinPoint joinPoint, Object response) {
        handlerLog(joinPoint, null, response);
    }

    /**
     * 当方法异常返回时执行
     *
     * @param joinPoint 切点
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handlerLog(joinPoint, e, null);
    }

    /**
     * 日志处理方法
     *
     * @param joinPoint 切点
     * @param e         异常
     * @param response  切面方法返回
     */
    private void handlerLog(JoinPoint joinPoint, Exception e, Object response) {
        long executeTime;
        try {
            executeTime = System.nanoTime() - startTimeLocal.get();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            HttpServletRequest request = ServletUtils.getRequest();
            // 获取日志注解
            Log log = method.getAnnotation(Log.class);
            LoginUserDetail loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null) {
                return;
            }
            if (log != null) {
                // 创建操作日志对象
                SysLog sysLog = new SysLog();
                sysLog.setCreateTime(new Date());
                sysLog.setModuleName(log.value().getName());
                sysLog.setOperation(log.operator().getCode());
                sysLog.setUserName(loginUser.getUsername());
                sysLog.setUrl(StringUtils.substring(request.getRequestURI(), 0, 100));
                // 设置方法名称
                String className = joinPoint.getTarget().getClass().getName();
                String methodName = method.getName();
                sysLog.setMethod(className + "." + methodName + "()");
                sysLog.setOperState(1);
                sysLog.setRequestMethod(request.getMethod());
                sysLog.setExecuteTime(executeTime / 1000000);
                // 保存请求响应
                if (Objects.nonNull(response)) {
                    sysLog.setRequestResponse(StringUtils.substring(JSON.toJSONString(response), 0, 2000));
                }
                // 判断是否需要保存请求参数
                if (log.isNeedParam()) {
                    String reqParmeter;
                    if (HttpUtil.isJsonRequest(request)) {
                        reqParmeter = ServletUtils.getBody(request);
                    } else {
                        // 保存请求参数
                        Map<String, String[]> parameterMap = request.getParameterMap();
                        JSONObject obj = new JSONObject(true);
                        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                            String key = entry.getKey();
                            String[] value = entry.getValue();
                            if (value.length == 1 && StringUtils.isNotEmpty(value[0])) {
                                obj.put(key, value[0]);
                            } else {
                                obj.put(key, value);
                            }
                        }
                        reqParmeter = obj.toJSONString();
                    }
                    sysLog.setRequestParams(StringUtils.substring(reqParmeter, 0, 2000));
                }
                if (e != null) {
                    sysLog.setOperState(0);
                    sysLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
                }
                iSysLogService.save(sysLog);
            }
        } catch (Exception exception) {
            log.error("handlerLog", exception);
        } finally {
            startTimeLocal.remove();
        }

    }
}

package com.hospital.booking.aop;

import com.alibaba.fastjson2.JSON;
import com.hospital.booking.annotation.Log;
import com.hospital.booking.entity.OperationLog;
import com.hospital.booking.servcie.IOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final IOperationLogService operationLogService;

    /**
     * 切点定义:所有带有@Log注解的方法
     */
    @Pointcut("@annotation(com.hospital.booking.annotation.Log)")
    public void logPointCut() {}

    /**
     * 环绕通知：在目标方法执行前后记录日志
     * @param point 连接点对象，包含目标方法信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point)throws Throwable{
        long begin = System.currentTimeMillis();
        //执行目标方法
        Object result = point.proceed();
        long end = System.currentTimeMillis();
        long time = end - begin;
        //记录日志
        saveLog(point,time,result);

        return result;
    }

    /**
     * 保存日志信息
     * @param joinPoint 连接点对象
     * @param time 方法执行耗时（毫秒）
     * @param result 方法返回结果
     */
    private void saveLog(ProceedingJoinPoint joinPoint,long time,Object result){
        try{

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            if(logAnnotation == null){
                return;
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            Long operatorId = getCurrentUserId(request);
            String operatorName = getCurrentUserName(request);

            OperationLog operationLog = new OperationLog();
            operationLog.setOperatorId(operatorId);
            operationLog.setOperatorName(operatorName);
            operationLog.setOperationType(logAnnotation.value());
            operationLog.setTargetType(logAnnotation.targetType());
            operationLog.setContent(buildContent(joinPoint, result, logAnnotation));
            operationLog.setIpAddress(getIpAddress(request));
            operationLog.setCreateTime(LocalDateTime.now());

            operationLogService.save(operationLog);
            log.info("操作日志记录成功：操作类型={}, 操作人={}", logAnnotation.value(), operatorName);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if(userId != null){
            return Long.valueOf(userId.toString());
        } else {
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUserName(HttpServletRequest request) {
        // 优先从Security上下文获取用户名
        org.springframework.security.core.Authentication authentication
                = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() !=null){
            Object principal = authentication.getPrincipal();
            if(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails){
                return userDetails.getUsername();
            } else {
                return principal.toString();
            }
        }
        // 其次从请求属性获取（兼容旧逻辑）
        Object username = request.getAttribute("username");
        if(username != null){
            return username.toString();
        }
        return "未知用户";
    }

    /**
     * 获取客户端IP地址
     * 支持多级代理场景
     */
    private String getIpAddress(HttpServletRequest request) {
        // 优先从X-Forwarded-For获取（经过代理的情况）
        String ip = request.getHeader("X-Forwarded-For");
        if(ip == null || ip.isEmpty()){
            // 其次从X-Real-IP获取
            ip = request.getHeader("X-Real-IP");
        }
        if(ip == null || ip.isEmpty()){
            // 最后从RemoteAddr获取
            ip = request.getRemoteAddr();
        }

        //多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 构建日志内容字符串
     * 包含方法名、请求参数和返回结果
     * @param joinPoint 连接点对象
     * @param result 方法返回结果
     * @param logAnnotation @Log注解对象
     * @return 日志内容字符串
     */
    private String buildContent(ProceedingJoinPoint joinPoint, Object result, Log logAnnotation) {
        StringBuilder content = new StringBuilder();

        // 添加方法名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        content.append("方法：").append(signature.getMethod().getName());

        // 如果配置了保存请求参数，则添加参数信息
        if (logAnnotation.saveRequestParam()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    content.append("，参数：").append(JSON.toJSONString(args));
                } catch (Exception e) {
                    content.append("，参数：解析失败");
                }
            }
        }

        // 如果配置了保存返回结果，则添加结果信息
        if (logAnnotation.saveResponseResult() && result != null) {
            try {
                content.append("，结果：").append(JSON.toJSONString(result));
            } catch (Exception e) {
                content.append("，结果：解析失败");
            }
        }

        return content.toString();

    }

}

package com.noahjiang.se.main.interceptor;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 自动为请求生成和管理链路追踪ID的拦截器
 * 无需在代码中显式嵌入traceId，自动在日志中打印
 */
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 检查请求头中是否已经有traceId，如果有则使用，没有则生成新的
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        // 将traceId设置到ThreadContext中，供log4j2使用
        ThreadContext.put(TRACE_ID, traceId);
        
        // 也将traceId添加到响应头中，便于后续请求传递
        response.setHeader(TRACE_ID, traceId);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 请求处理完成后不需要特殊处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清理ThreadContext，避免内存泄漏
        ThreadContext.remove(TRACE_ID);
    }

    /**
     * 生成唯一的traceId
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
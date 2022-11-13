package com.example;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.example.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;


/**
 * Authentication Interceptor
 *
 * @author weasley
 * @version 1.0.0
 */
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    protected static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestUri = request.getRequestURI();

        Enumeration<String> headerNames = request.getHeaderNames();
        String tokenValue = null;
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("获取到请求头 {}: {}", headerName, headerValue);
        }

        boolean login = StpUtil.isLogin();
        if (!login) {
            Result<Object> result = Result.error(99999, "Invalid Token" + ": " + StpUtil.getTokenValue());
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.println(JSONUtil.toJsonStr(result));
            writer.flush();
            writer.close();
            return false;
        }

        return true;
    }
}

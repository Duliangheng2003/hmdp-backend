package com.liangheng.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
            throws Exception {
        // 判断是否需要拦截
        if (UserHolder.getUser() == null){
            // 没有用户信息，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截请求
            return false;
        }
        // 用户信息存在，放行请求
        return true;
    }
}



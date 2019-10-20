package com.hanger.component;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * @author hanger
 * 2019-08-27 12:04
 */
//@Component
public class LoginInterceptor implements HandlerInterceptor {

    //在所有接口调用之前调用(true表示已经登录可以访问controller,false表示未登录)
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();

        if (session.getAttribute("LOGIN_USER") == null) {
            request.setAttribute("msg", "没有权限请先登录");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {

    }


}










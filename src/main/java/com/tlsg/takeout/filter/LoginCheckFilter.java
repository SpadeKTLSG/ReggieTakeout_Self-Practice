package com.tlsg.takeout.filter;

import com.alibaba.fastjson.JSON;
import com.tlsg.takeout.common.BaseContext;
import com.tlsg.takeout.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;


import java.io.IOException;


@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*") //拦截所有请求,返回登录页面
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest hsr = (HttpServletRequest) servletRequest;
        HttpServletResponse hsp = (HttpServletResponse) servletResponse;

        //静默通知
        //log.info("SK拦截到请求: {}", hsr.getRequestURI()); //使用{}占位符, 后面的参数会替换占位符, 这里会将hsr.getRequestURI()的返回值替换到{}中

        String requestURI = hsr.getRequestURI(); //获取本次请求的URI


        //定义不需要处理的请求路径(页面想看就看)
        String[] safe_urls = new String[]{
                "/employee/login", //登录
                "/employee/logout", //登出
                "/backend/**", //后台
                "/front/**", //前台
                "/common/**",//公共
                "/user/sendMsg", //发送短信
                "/user/login" //用户登录
        };


        //2、判断本次请求是否需要处理
        boolean need = check(safe_urls, requestURI);

        //3、如果不需要处理，则直接放行
        if (need) {
//            log.info("本次请求{}不需要处理", requestURI); //静默通知
            filterChain.doFilter(hsr, hsp); //放行
            return;
        }

        //4、判断员工登录状态，如果已登录，则直接放行
        if (hsr.getSession().getAttribute("employee") != null) { //从Session中获取employee属性, 如果不为空则说明已经登录
//            log.info("id为：{}的员工已登录!", hsr.getSession().getAttribute("employee"));//静默通知

            //将当前登录用户的id存入ThreadLocal中
            BaseContext.setCurrentId((Long) hsr.getSession().getAttribute("employee"));

            filterChain.doFilter(hsr, hsp); //放行
            return;
        }

        //4- 判断用户登录状态，如果已登录，则直接放行
        if (hsr.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}", hsr.getSession().getAttribute("user"));

            //将当前登录用户的id存入ThreadLocal中
            BaseContext.setCurrentId((Long) hsr.getSession().getAttribute("user"));

            filterChain.doFilter(hsr, hsp);
            return;
        }


        //如果未登录则返回未登录结果; 通过输出流方式向客户端页面响应数据
        log.info("用户未登录!");
        hsp.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    //判断本次请求是否需要处理
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) { //逐个匹配
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}

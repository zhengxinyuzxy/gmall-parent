package com.atguigu.gmall.user.filter;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.user.util.GmallThreadLocalUtils;
import org.springframework.core.annotation.Order;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 过滤器
 */
@Order(1)
@WebFilter(filterName = "appTokenFilter", urlPatterns = "/*")
public class AppTokenFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        //强制类型转换
        HttpServletRequest request = (HttpServletRequest)req;
        String token = request.getHeader("Authorization");
        if(!StringUtils.isEmpty(token)){
            token = token.replace("bearer ", "");
            String claims = JwtHelper.decode(token).getClaims();
            Map map = JSONObject.parseObject(claims, Map.class);
            Object username = map.get("username");
            if(username != null){
                GmallThreadLocalUtils.setUserName(username.toString());
            }
        }
        chain.doFilter(req,res);
    }
}
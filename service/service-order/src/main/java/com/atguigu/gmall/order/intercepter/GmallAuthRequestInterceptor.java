package com.atguigu.gmall.order.intercepter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class GmallAuthRequestInterceptor implements RequestInterceptor {

    /**
     * 发起feign调用以前,将接收请求的线程的所有参数都存入feign调用的请求头中去
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //使用servlet提供的对象获取全局的request
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        //非空判断
        if(servletRequestAttributes != null){
            //获取原线程的请求对象
            HttpServletRequest request = servletRequestAttributes.getRequest();
            //获取请求头
            Enumeration<String> headerNames = request.getHeaderNames();
            //循环遍历
            while (headerNames.hasMoreElements()){
                //获取当前请求头中的某个key
                String s = headerNames.nextElement();
                //获取这个key对应的value
                String value = request.getHeader(s);
                //存入feign的request
                requestTemplate.header(s, value);
            }
        }
    }
}
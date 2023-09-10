package com.atguigu.gmall.oauth.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/user/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private HttpServletRequest request;
    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping
    public Result login(String username, String password){
        //登录获取令牌
        AuthToken authToken = loginService.login(username, password);
        //将令牌的信息存储到redis中去
        String ipAddress = IpUtil.getIpAddress(request);
        stringRedisTemplate.opsForValue().set(ipAddress, authToken.getAccessToken());
        //返回用户令牌的信息
        return Result.ok(authToken);
    }
}

package com.atguigu.gmall.oauth.service.impl;

import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;
    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public AuthToken login(String username, String password) {
        //参数校验
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return null;
        }
        //初始化请求头
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Authorization", getHeadParam());
        //初始化body
        MultiValueMap<String, String> body = new HttpHeaders();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        //包装参数
        HttpEntity httpEntity = new HttpEntity(body, headers);

        //发起请求进行登录,获取返回结果
//        String url = "http://localhost:9001/oauth/token";
        ServiceInstance choose = loadBalancerClient.choose("service-oauth");
        String s = choose.getUri().toString();
        String url = s + "/oauth/token";
        /**
         * 1.请求的地址是什么?
         * 2.请求的方法类型是什么
         * 3.请求的参数是什么
         * 4.请求返回结果的类型是什么
         *
         */
        ResponseEntity<Map> exchange =
                restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        Map<String,String> result = exchange.getBody();
        //解析令牌
        AuthToken authToken = new AuthToken();
        String accessToken = result.get("access_token");
        authToken.setAccessToken(accessToken);
        String refreshToken = result.get("refresh_token");
        authToken.setRefreshToken(refreshToken);
        String jti = result.get("jti");
        authToken.setJti(jti);
        //返回令牌
        return authToken;
    }

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;
    /**
     * 获取请求头中的参数
     * @return
     */
    private String getHeadParam() {
        //进行拼接
        String param = clientId + ":" + clientSecret;
        //进行base64加密
        byte[] encode = Base64.getEncoder().encode(param.getBytes());
        //转换为字符串
        String s = new String(encode);
        //返回结果
        return "Basic " + s;
    }
}

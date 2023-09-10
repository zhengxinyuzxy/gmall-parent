package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.gateway.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关的全局过滤器
 */
@Component
public class GmallFilter implements GlobalFilter, Ordered {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 过滤器的自定义逻辑
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();
        //获取response
        ServerHttpResponse response = exchange.getResponse();
        //获取token令牌,从url中获取
        String token = request.getQueryParams().getFirst("token");
        if(StringUtils.isEmpty(token)){
            //url中没有token,从请求头中获取
            token = request.getHeaders().getFirst("token");
            if(StringUtils.isEmpty(token)){
                //请求头也没有,从cookie中取
                HttpCookie cookie = request.getCookies().getFirst("token");
                if(cookie != null){
                    token = cookie.getValue();
                }
            }
        }
        //判断token是否为空
        if(StringUtils.isEmpty(token)){
            //用户请求没有携带token,拒绝请求
            response.setStatusCode(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
            return response.setComplete();
        }
        //token存在,校验令牌是否被盗用!
        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        //从redis中获取令牌
        String redisToken = stringRedisTemplate.opsForValue().get(gatwayIpAddress);
        if(StringUtils.isEmpty(redisToken)){
            //用户请求没有携带token,拒绝请求
            response.setStatusCode(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
            return response.setComplete();
        }
        //判断令牌是否一致
        if(!redisToken.equals(token)){
            //用户请求没有携带token,拒绝请求
            response.setStatusCode(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
            return response.setComplete();
        }
        //正常情况,将token存入请求头
        request.mutate().header("Authorization", "bearer " + token);
        //放行
        return chain.filter(exchange);
    }

    /**
     * 过滤器的执行顺序web.xml
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}

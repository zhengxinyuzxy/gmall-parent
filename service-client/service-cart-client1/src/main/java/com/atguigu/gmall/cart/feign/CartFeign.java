package com.atguigu.gmall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "service-cart", path = "/api/cart")
public interface CartFeign {

    /**
     * 下单使用的查询接口
     * @return
     */
    @GetMapping(value = "/getAddOrderInfo")
    public Map<String, Object> getAddOrderInfo();

    /**
     * 下单完成后清空当前购买的商品的购物车数据
     * @return
     */
    @GetMapping(value = "/delCartInfo")
    public Boolean delCartInfo();
}

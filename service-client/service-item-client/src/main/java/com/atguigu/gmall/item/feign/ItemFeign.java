package com.atguigu.gmall.item.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情页前端页面使用的feign接口
 */
@FeignClient(name = "service-item" , path = "/client/item")
public interface ItemFeign {

    /**
     * 查询商品详情
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuItem/{skuId}")
    public Map<String, Object> getSkuItem(@PathVariable(value = "skuId") Long skuId);
}

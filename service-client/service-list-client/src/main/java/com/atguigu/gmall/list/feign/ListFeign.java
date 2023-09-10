package com.atguigu.gmall.list.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 搜索微服务的feign接口
 */
@FeignClient(name = "service-list", path = "/api/list")
public interface ListFeign {

    /**
     * 商品上架:将商品的数据写入到es中去
     * @param skuId
     * @return
     */
    @GetMapping(value = "/upper/{skuId}")
    public void upper(@PathVariable(value = "skuId") Long skuId);

    /**
     * 商品的下架
     * @param skuId
     * @return
     */
    @GetMapping(value = "/down/{skuId}")
    public void down(@PathVariable(value = "skuId") Long skuId);

    /**
     * 商品搜索
     * @param searchMap
     * http://localhost:8203/api/list/search?keywords=手机&category=1
     * @return
     */
    @GetMapping(value = "/search")
    public Map<String, Object> search(@RequestParam Map<String, String> searchMap);
}

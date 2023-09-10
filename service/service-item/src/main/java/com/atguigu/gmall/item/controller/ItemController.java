package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品详情的controller
 */
@RestController
@RequestMapping(value = "/client/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 查询商品详情
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuItem/{skuId}")
    public Map<String, Object> getSkuItem(@PathVariable(value = "skuId") Long skuId){
        return itemService.getSkuItem(skuId);
    }


}

package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/list")
public class ListController {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 创建索引和映射以及类型
     * @return
     */
    @GetMapping(value = "/create")
    public Result createIndexAndMapping(){
        //创建索引
        elasticsearchRestTemplate.createIndex(Goods.class);
        //创建映射
        elasticsearchRestTemplate.putMapping(Goods.class);

        return Result.ok();
    }

    @Autowired
    private ListService listService;
    /**
     * 商品上架:将商品的数据写入到es中去
     * @param skuId
     * @return
     */
    @GetMapping(value = "/upper/{skuId}")
    public void upper(@PathVariable(value = "skuId") Long skuId){
        listService.goodsUpper(skuId);
    }

    /**
     * 商品的下架
     * @param skuId
     * @return
     */
    @GetMapping(value = "/down/{skuId}")
    public void down(@PathVariable(value = "skuId") Long skuId){
        listService.goodsDown(skuId);
    }

    /**
     * 更新热点值
     * @param skuId
     * @return
     */
    @GetMapping(value = "/addScore/{skuId}")
    public Result addScore(@PathVariable(value = "skuId") Long skuId){
        listService.addScore(skuId);
        return Result.ok();
    }

    @Autowired
    private SearchService searchService;
    /**
     * 商品搜索
     * @param searchMap
     * http://localhost:8203/api/list/search?keywords=手机&category=1
     * @return
     */
    @GetMapping(value = "/search")
    public Map<String, Object> search(@RequestParam Map<String, String> searchMap){
        return searchService.search(searchMap);
    }
}

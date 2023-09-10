package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 内部使用的商品详情页的api接口
 */
@RestController
@RequestMapping(value = "/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 前置增强--查缓存--没有
     * 后置增强--获取查询数据库的结果,存入缓存
     * 环绕增强
     */
    /**
     * 查询sku的详细信息
     * @param skuId
     * @return
     */
    @GmallCache(prefix = "getSkuInfo:")
    @GetMapping(value = "/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(value = "skuId") Long skuId){
        return itemService.getSkuInfo(skuId);
    }

    /**
     * 根据三级分类查询一级二级三级分类的详细信息
     * @param category3Id
     * @return
     */
    @GmallCache(prefix = "getBaseCategoryView:")
    @GetMapping(value = "/getBaseCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryView(@PathVariable(value = "category3Id") Long category3Id){
        return itemService.getBaseCategoryView(category3Id);
    }

    /**
     * 根据sku的id查询该sku的所有图片
     * @param skuId
     * @return
     */
    @GmallCache(prefix = "getSkuImageList:")
    @GetMapping(value = "/getSkuImageList/{skuId}")
    public List<SkuImage> getSkuImageList(@PathVariable(value = "skuId") Long skuId){
        return itemService.getSkuImageList(skuId);
    }

    /**
     * 查询sku的价格
     * @param skuId
     * @return
     */
    @GmallCache(prefix = "getPrice:")
    @GetMapping(value = "/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable(value = "skuId") Long skuId){
        return itemService.getPrice(skuId);
    }

    /**
     * 根据spu和sku的id查询所有的销售属性和销售属性值,并且标注出当前的sku是哪个
     *
     * @param spuId
     * @param skuId
     * @return
     */
    @GmallCache(prefix = "getSpuSaleAttrBySpuIdAndSkuId:")
    @GetMapping(value = "/getSpuSaleAttrBySpuIdAndSkuId/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(@PathVariable(value = "spuId")Long spuId,
                                                           @PathVariable(value = "skuId")Long skuId){
        return itemService.getSpuSaleAttrBySpuIdAndSkuId(spuId, skuId);
    }

    /**
     * 根据spu的id查询该spu下所有sku的销售属性的键值对
     * @param spuId
     * @return
     */
    @GmallCache(prefix = "getSkuSaleValueKeys:")
    @GetMapping(value = "/getSkuSaleValueKeys/{spuId}")
    public Map getSkuSaleValueKeys(@PathVariable(value = "spuId") Long spuId){
        return itemService.getSkuSaleValueKeys(spuId);
    }

    /**
     * 查询品牌的详情
     * @param id
     * @return
     */
    @GetMapping(value = "/getBaseTrademark/{id}")
    @GmallCache(prefix = "getBaseTrademark:")
    public BaseTrademark getBaseTrademark(@PathVariable(value = "id") Long id){
        return itemService.getBaseTrademark(id);
    }

    /**
     * 根据sku的id查询销售属性列表
     *
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuBaseAttrInfoBySkuId/{skuId}")
    @GmallCache(prefix = "getSkuBaseAttrInfoBySkuId:")
    public List<BaseAttrInfo> getSkuBaseAttrInfoBySkuId(@PathVariable(value = "skuId") Long skuId){
        return itemService.getSkuBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 扣减库存
     * @param map
     * @return
     */
    @GetMapping(value = "/decountStock")
    public Boolean decountStock(@RequestParam Map<String,String> map){
        return itemService.decountStock(map);
    }

    /**
     * 回滚库存
     * @param map
     * @return
     */
    @GetMapping(value = "/rollbackStock")
    public Boolean rollbackStock(@RequestParam Map<String,String> map){
        return itemService.decountStock(map);
    }

}

package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    /**
     * 获取商品详情的数据
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getSkuItem(Long skuId) {
        //参数校验
        if(skuId == null){
            throw new RuntimeException("商品不存在!");
        }
        //初始化返回结果
        Map<String, Object> map = new HashMap<>();
        //查询sku_info表的数据   1s
        CompletableFuture<SkuInfo> future1 = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
            if(skuInfo == null || skuInfo.getId() == null){
                return null;
            }
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        //查询sku所属的分类的信息:一级二级三级分类的id和name  2s
        CompletableFuture<Void> future2 = future1.thenAcceptAsync((skuInfo) -> {
            if(skuInfo == null || skuInfo.getId() == null){
                return;
            }
            BaseCategoryView baseCategoryView =
                    productFeign.getBaseCategoryView(skuInfo.getCategory3Id());
            map.put("baseCategoryView", baseCategoryView);
        }, threadPoolExecutor);
        //查询sku的图片列表1s
        CompletableFuture<Void> future3 = future1.thenAcceptAsync((skuInfo) -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            List<SkuImage> skuImageList = productFeign.getSkuImageList(skuInfo.getId());
            map.put("skuImageList", skuImageList);
        }, threadPoolExecutor);
        //查询sku的价格信息  1s
        CompletableFuture<Void> future4 = future1.thenAcceptAsync((skuInfo -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            BigDecimal price = productFeign.getPrice(skuInfo.getId());
            map.put("price", price);
        }), threadPoolExecutor);
        //查询sku所属的spu的所有的销售属性和销售属性的值的信息,标识出当前的sku是哪一个 1s
        CompletableFuture<Void> future5 = future1.thenAcceptAsync((skuInfo -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            List<SpuSaleAttr> spuSaleAttrList =
                    productFeign.getSpuSaleAttrBySpuIdAndSkuId(skuInfo.getSpuId(), skuInfo.getId());
            map.put("spuSaleAttrList", spuSaleAttrList);
        }), threadPoolExecutor);
        //根据spu的id查询该spu下所有sku的销售属性的键值对 1s
        CompletableFuture<Void> future6 = future1.thenAcceptAsync((skuInfo -> {
            if (skuInfo == null || skuInfo.getId() == null) {
                return;
            }
            Map keysMap = productFeign.getSkuSaleValueKeys(skuInfo.getSpuId());
            map.put("keysMap", keysMap);
        }), threadPoolExecutor);
        //等待所有的都运行完成以后再进行返回
        CompletableFuture.allOf(future1, future2, future3, future4, future5, future6).join();
        //返回结果
        return map;
    }
}

package com.atguigu.gmall.product.feign;

import com.atguigu.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品管理微服务的内容调用使用的feign接口
 */
@FeignClient(name = "service-product", path = "/api/item")
public interface ProductFeign {

    /**
     * 查询sku的详细信息
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(value = "skuId") Long skuId);

    /**
     * 根据三级分类查询一级二级三级分类的详细信息
     * @param category3Id
     * @return
     */
    @GetMapping(value = "/getBaseCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryView(@PathVariable(value = "category3Id") Long category3Id);

    /**
     * 根据sku的id查询该sku的所有图片
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuImageList/{skuId}")
    public List<SkuImage> getSkuImageList(@PathVariable(value = "skuId") Long skuId);

    /**
     * 查询sku的价格
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable(value = "skuId") Long skuId);

    /**
     * 根据spu和sku的id查询所有的销售属性和销售属性值,并且标注出当前的sku是哪个
     *
     * @param spuId
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSpuSaleAttrBySpuIdAndSkuId/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(@PathVariable(value = "spuId")Long spuId,
                                                           @PathVariable(value = "skuId")Long skuId);

    /**
     * 根据spu的id查询该spu下所有sku的销售属性的键值对
     * @param spuId
     * @return
     */
    @GetMapping(value = "/getSkuSaleValueKeys/{spuId}")
    public Map getSkuSaleValueKeys(@PathVariable(value = "spuId") Long spuId);

    /**
     * 查询品牌的详情
     * @param id
     * @return
     */
    @GetMapping(value = "/getBaseTrademark/{id}")
    public BaseTrademark getBaseTrademark(@PathVariable(value = "id") Long id);

    /**
     * 根据sku的id查询销售属性列表
     *
     * @param skuId
     * @return
     */
    @GetMapping(value = "/getSkuBaseAttrInfoBySkuId/{skuId}")
    public List<BaseAttrInfo> getSkuBaseAttrInfoBySkuId(@PathVariable(value = "skuId") Long skuId);

    /**
     * 扣减库存
     * @param map
     * @return
     */
    @GetMapping(value = "/decountStock")
    public Boolean decountStock(@RequestParam Map<String,String> map);

    /**
     * 回滚库存
     * @param map
     * @return
     */
    @GetMapping(value = "/rollbackStock")
    public Boolean rollbackStock(@RequestParam Map<String,String> map);
}

package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品详情页使用的内部接口类
 */
public interface ItemService {

    /**
     * 根据id查询sku的详细信息
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据id查询sku的详细信息redis单点方案
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoFromRedisOrDb(Long skuId);

    /**
     * 根据id查询sku的详细信息redis集群方案
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoFromRedission(Long skuId);


    /**
     * 根据三级分类查询一级二级三级分类的详细信息
     * @param category3Id
     * @return
     */
    public BaseCategoryView getBaseCategoryView(Long category3Id);

    /**
     * 根据sku的id查询该sku的所有图片
     * @param skuId
     * @return
     */
    public List<SkuImage> getSkuImageList(Long skuId);

    /**
     * 查询sku的价格
     * @param skuId
     * @return
     */
    public BigDecimal getPrice(Long skuId);

    /**
     * 根据spu和sku的id查询所有的销售属性和销售属性值,并且标注出当前的sku是哪个
     * @param spuId
     * @param skuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(Long spuId,
                                                           Long skuId);

    /**
     * 根据spu的id查询该spu下所有sku的销售属性的键值对
     * @param spuId
     * @return
     */
    public Map getSkuSaleValueKeys(Long spuId);

    /**
     * 查询品牌的详情
     * @param id
     * @return
     */
    public BaseTrademark getBaseTrademark(Long id);

    /**
     * 根据sku的id查询销售属性列表
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> getSkuBaseAttrInfoBySkuId(Long skuId);

    /**
     * 扣减库存
     * @param map
     */
    public Boolean decountStock(Map<String,String> map);

    /**
     * 回滚库存
     * @param map
     */
    public Boolean rollbackStock(Map<String,String> map);
}

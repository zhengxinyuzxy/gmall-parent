package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 管理控制台使用的api接口的service
 */
public interface ManageService {

    /**
     * 查询所有的一级分类
     * @return
     */
    public List<BaseCategory1> getCategory1();

    /**
     * 根据一级级分类查询二级分类
     * @param cid
     * @return
     */
    public List<BaseCategory2> getCategory2(Long cid);

    /**
     * 根据二级分类查询三级分类
     * @param cid
     * @return
     */
    public List<BaseCategory3> getCategory3(Long cid);

    /**
     * 保存平台属性
     * @param baseAttrInfo
     */
    public BaseAttrInfo saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据分类信息查询平台属性的列表
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfoByCategory(Long category1,
                                                        Long category2,
                                                        Long category3);

    /**
     * 根据平台属性名称的id查询平台属性值的列表
     * @param attrId
     * @return
     */
    public List<BaseAttrValue> getBaseAttrValue(Long attrId);

    /**
     * 查询品牌的列表
     * @return
     */
    public List<BaseTrademark> getBaseTradeMark();

    /**
     * 获取所有的销售属性列表
     * @return
     */
    public List<BaseSaleAttr> getBaseSaleAttr();

    /**
     * 保存spu信息
     * @param spuInfo
     */
    public SpuInfo saveSpuInfo(SpuInfo spuInfo);

    /**
     * 分页查询spu列表的信息
     * @param category3Id
     * @param page
     * @param size
     * @return
     */
    public IPage<SpuInfo> getSpuInfoList(Long category3Id, Integer page, Integer size);

    /**
     * 根据spu的id查询所有的销售属性
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttr(Long spuId);

    /**
     * 根据spuid查询所有的图片列表
     * @param spuId
     * @return
     */
    public List<SpuImage> getSpuImageList(Long spuId);

    /**
     * 保存sku的信息
     * @param skuInfo
     * @return
     */
    public SkuInfo saveSkuInfo(SkuInfo skuInfo);

    /**
     * 数据库中:商品上架或下架
     * @param skuId
     * @param status
     * @return
     */
    public SkuInfo onSaleOrOff(Long skuId, Short status);
}

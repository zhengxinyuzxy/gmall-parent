package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/admin/product")
public class MangeController {

    @Autowired
    private ManageService manageService;

    /**
     * 查询所有的一级分类
     * @return
     */
    @GetMapping(value = "/getCategory1")
    public Result getCategory1(){
        return Result.ok(manageService.getCategory1());
    }

    /**
     * 根据一级分类查询二级分类
     * @param cid
     * @return
     */
    @GetMapping(value = "/getCategory2/{cid}")
    public Result getCategory2(@PathVariable(value = "cid") Long cid){
        return Result.ok(manageService.getCategory2(cid));
    }

    /**
     * 根据二级分类查询三级分类
     * @param cid
     * @return
     */
    @GetMapping(value = "/getCategory3/{cid}")
    public Result getCategory3(@PathVariable(value = "cid") Long cid){
        return Result.ok(manageService.getCategory3(cid));
    }

    /**
     * 保存平台属性信息
     * @param baseAttrInfo
     * @return
     */
    @PostMapping(value = "/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        return Result.ok(manageService.saveAttrInfo(baseAttrInfo));
    }

    /**
     * 根据分类信息查询平台属性的列表
     *
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    @GetMapping(value = "/attrInfoList/{category1}/{category2}/{category3}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable(value = "category1") Long category1,
                                                                @PathVariable(value = "category2") Long category2,
                                                                @PathVariable(value = "category3") Long category3) {
        return Result.ok(manageService.getBaseAttrInfoByCategory(category1, category2, category3));
    }

    /**
     * 根据平台属性名称的id查询平台属性值的列表
     * @param attrId
     * @return
     */
    @GetMapping(value = "/getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getBaseAttrValue(@PathVariable(value = "attrId") Long attrId){
        return Result.ok(manageService.getBaseAttrValue(attrId));
    }

    /**
     * 查询所有的品牌列表
     * @return
     */
    @GetMapping(value = "/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        return Result.ok(manageService.getBaseTradeMark());
    }


    /**
     * 获取所有的销售属性列表
     * @return
     */
    @GetMapping(value = "/baseSaleAttrList")
    public Result baseSaleAttrList(){
        return Result.ok(manageService.getBaseSaleAttr());
    }

    /**
     * 保存spu的信息/修改spu的信息
     * @param spuInfo
     * @return
     */
    @PostMapping(value = "/saveSpuInfo")
    public Result<SpuInfo> saveSpuInfo(@RequestBody SpuInfo spuInfo){
        return Result.ok(manageService.saveSpuInfo(spuInfo));
    }

    /**
     * 分页查询spu的列表信息
     * @param category3Id
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/{page}/{size}")
    public Result getSpuInfo(Long category3Id,
                             @PathVariable(value = "page") Integer page,
                             @PathVariable(value = "size") Integer size){
        return Result.ok(manageService.getSpuInfoList(category3Id, page, size));
    }

    /**
     * 根据spuid查询销售属性列表
     * @param spuId
     * @return
     */
    @GetMapping(value = "/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable(value = "spuId") Long spuId){
        return Result.ok(manageService.getSpuSaleAttr(spuId));
    }

    /**
     * 根据spuid查询所有的图片列表
     * @param spuId
     * @return
     */
    @GetMapping(value = "/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable(value = "spuId") Long spuId){
        return Result.ok(manageService.getSpuImageList(spuId));
    }

    /**
     * 保存sku的信息
     * @param skuInfo
     * @return
     */
    @PostMapping(value = "/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        return Result.ok(manageService.saveSkuInfo(skuInfo));
    }


    /**
     * 商品上架
     * @param skuId
     * @return
     */
    @GetMapping(value = "/onSale/{skuId}")
    public Result onSale(@PathVariable(value = "skuId") Long skuId){
        return Result.ok(manageService.onSaleOrOff(skuId, ProductConst.SKU_ON_SALE));
    }

    /**
     * 商品下架
     * @param skuId
     * @return
     */
    @GetMapping(value = "/offSale/{skuId}")
    public Result offSale(@PathVariable(value = "skuId") Long skuId){
        return Result.ok(manageService.onSaleOrOff(skuId, ProductConst.SKU_OFF_SALE));
    }
}

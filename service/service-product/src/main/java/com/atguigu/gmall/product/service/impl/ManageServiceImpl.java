package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.list.feign.ListFeign;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManageServiceImpl implements ManageService {

    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;
    /**
     * 查询所有的一级分类
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }


    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;
    /**
     * 根据一级分类查询二级分类
     *
     * @param cid
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long cid) {
        //参数校验
        if(cid == null){
            throw new RuntimeException("参数错误!");
        }
        //构建查询条件
        LambdaQueryWrapper<BaseCategory2> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategory2::getCategory1Id, cid);
        //执行查询返回结果
        return baseCategory2Mapper.selectList(wrapper);
    }

    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;
    /**
     * 根据二级分类查询三级分类
     *
     * @param cid
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long cid) {
        //参数校验
        if(cid == null){
            throw new RuntimeException("参数错误!");
        }
        //构建查询条件
        LambdaQueryWrapper<BaseCategory3> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategory3::getCategory2Id, cid);
        //执行查询返回结果
        return baseCategory3Mapper.selectList(wrapper);
    }


    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;
    /**
     * 保存平台属性
     *
     * @param baseAttrInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseAttrInfo saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //参数校验
        if(baseAttrInfo == null){
            throw new RuntimeException("参数错误!");
        }
        //判断平台属性名称的id是否存在
        if(baseAttrInfo.getId() != null){
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //删除平台属性值
            baseAttrValueMapper.delete(
                    new LambdaQueryWrapper<BaseAttrValue>().eq(BaseAttrValue::getAttrId, baseAttrInfo.getId()));
        }else{
            //保存平台属性的名称
            int insert = baseAttrInfoMapper.insert(baseAttrInfo);
            if(insert <= 0 ){
                throw new RuntimeException("保存平台属性名称错误,请重试!");
            }
        }
        //获取所有的平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//        //补全数据--方案一
//        for (BaseAttrValue baseAttrValue : attrValueList) {
//            //补全平台属性名称的id
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            //保存数据
//            baseAttrValueMapper.insert(baseAttrValue);
//        }
        //方案二,数据流,所有的value都具有id了
        List<BaseAttrValue> attrValueListNew = attrValueList.stream().map(baseAttrValue -> {
            //补全数据
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            //新增
            baseAttrValueMapper.insert(baseAttrValue);
            //返回
            return baseAttrValue;
        }).collect(Collectors.toList());
        //替换旧数据
        baseAttrInfo.setAttrValueList(attrValueListNew);
        //返回
        return baseAttrInfo;
    }


    /**
     * 根据分类信息查询平台属性的列表
     *
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCategory(Long category1,
                                                        Long category2,
                                                        Long category3) {
        return baseAttrInfoMapper.selectBaseAttrInfoByCategory(category1,category2,category3);
    }

    /**
     * 根据平台属性名称的id查询平台属性值的列表
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getBaseAttrValue(Long attrId) {
        return baseAttrValueMapper.selectList(
                new LambdaQueryWrapper<BaseAttrValue>().eq(BaseAttrValue::getAttrId, attrId));
    }

    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;
    /**
     * 查询品牌的列表
     *
     * @return
     */
    @Override
    public List<BaseTrademark> getBaseTradeMark() {
        return baseTradeMarkMapper.selectList(null);
    }


    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;
    /**
     * 获取所有的销售属性列表
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttr() {
        return baseSaleAttrMapper.selectList(null);
    }


    @Resource
    private SpuInfoMapper spuInfoMapper;

    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Resource
    private SpuImageMapper spuImageMapper;
    /**
     * 保存spu信息
     *
     * @param spuInfo
     */
    @Override
    public SpuInfo saveSpuInfo(SpuInfo spuInfo) {
        //参数校验
        if(spuInfo == null){
            throw new RuntimeException("参数错误");
        }
        //判断spuinfo中id是否为空,如果id不为空说明是修改
        if(spuInfo.getId() != null){
            //修改
            spuInfoMapper.updateById(spuInfo);
            //删除旧数据1.删除图片数据,条件:等于当前spuid
            spuImageMapper.delete(
                    new LambdaQueryWrapper<SpuImage>()
                            .eq(SpuImage::getSpuId, spuInfo.getId()));
            //2.删除销售属性名表的数据,条件:等于当前spuid
            spuSaleAttrMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttr>()
                    .eq(SpuSaleAttr::getSpuId, spuInfo.getId()));
            //3.删除销售属性值表的数据,条件:等于当前spuid
            spuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttrValue>()
                            .eq(SpuSaleAttrValue::getSpuId, spuInfo.getId()));
        }else{
            //新增
            spuInfoMapper.insert(spuInfo);
        }
        //保存spu_image表的信息,补全spuid
        List<SpuImage> spuImages =
                saveSpuImageList(spuInfo.getSpuImageList(), spuInfo.getId());
        spuInfo.setSpuImageList(spuImages);
        //保存spu_sale_attr的信息,补全spuId
        List<SpuSaleAttr> spuSaleAttrs =
                saveSpuAttrList(spuInfo.getSpuSaleAttrList(), spuInfo.getId());
        spuInfo.setSpuSaleAttrList(spuSaleAttrs);
        //结束
        return spuInfo;
    }

    /**
     * 分页查询spu列表的信息
     *  @param category3Id
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoList(Long category3Id, Integer page, Integer size) {
        return spuInfoMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<SpuInfo>().eq(SpuInfo::getCategory3Id, category3Id)
                );
    }

    /**
     * 保存销售属性信息
     * @param spuSaleAttrList
     */
    private List<SpuSaleAttr> saveSpuAttrList(List<SpuSaleAttr> spuSaleAttrList, Long spuId) {
        return spuSaleAttrList.stream().map(spuSaleAttr -> {
            //补全spuId
            spuSaleAttr.setSpuId(spuId);
            //保存销售属性名称数据,保存完成以后,id就有值了
            spuSaleAttrMapper.insert(spuSaleAttr);
            //还要保存该销售属性名称对应的值的列表的信息
            List<SpuSaleAttrValue> spuSaleAttrValues = saveSpuSaleAttrValue(spuSaleAttr);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
            //返回
            return spuSaleAttr;
        }).collect(Collectors.toList());
    }

    /**
     * 保存销售属性值的信息
     * @param spuSaleAttr
     */
    private List<SpuSaleAttrValue> saveSpuSaleAttrValue(SpuSaleAttr spuSaleAttr) {
        return spuSaleAttr.getSpuSaleAttrValueList().stream().map(spuSaleAttrValue -> {
            //补全spuId
            spuSaleAttrValue.setSpuId(spuSaleAttr.getSpuId());
            //补全销售属性名称
            spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
            //保存
            spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            //返回
            return spuSaleAttrValue;
        }).collect(Collectors.toList());
    }


    /**
     * 保存spu的图片信息
     * @param spuImageList
     */
    private List<SpuImage> saveSpuImageList(List<SpuImage> spuImageList, Long spuId) {
        //补全spu的id并且保存图片的信息
        return spuImageList.stream().map(image ->{
            //补全spuid
            image.setSpuId(spuId);
            //保存
            spuImageMapper.insert(image);
            //返回
            return image;
        }).collect(Collectors.toList());
    }

    /**
     * 根据spu的id查询所有的销售属性
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttr(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrBySpuId(spuId);
    }

    /**
     * 根据spuid查询所有的图片列表
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        return spuImageMapper.selectList(
                new LambdaQueryWrapper<SpuImage>().eq(SpuImage::getSpuId, spuId));
    }


    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    /**
     * 保存sku的信息
     *
     * @param skuInfo
     * @return
     */
    @Override
    public SkuInfo saveSkuInfo(SkuInfo skuInfo) {
        //参数校验
        if(skuInfo == null){
            throw new RuntimeException("参数错误!");
        }
        //判断sku的id是否为空,若不为空则为修改
        if(skuInfo.getId() != null){
            //修改
            skuInfoMapper.updateById(skuInfo);
            //清理旧数据1图片 2 平台属性 3销售属性 ---条件都是skuid等于当前sku的id
            skuImageMapper.delete(
                    new LambdaQueryWrapper<SkuImage>()
                            .eq(SkuImage::getSkuId, skuInfo.getId()));
            skuAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuAttrValue>()
                            .eq(SkuAttrValue::getSkuId, skuInfo.getId()));
            skuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SkuSaleAttrValue>()
                            .eq(SkuSaleAttrValue::getSkuId, skuInfo.getId()));
        }else{
            //新增,新增完成以后,sku的id就有值了
            skuInfoMapper.insert(skuInfo);
        }
        //保存skuimage表的信息
        List<SkuImage> skuImageList =
                saveSkuImage(skuInfo.getSkuImageList(), skuInfo.getId());
        skuInfo.setSkuImageList(skuImageList);
        //保存sku平台属性表的信息
        List<SkuAttrValue> skuAttrValues =
                saveSkuAttrInfo(skuInfo.getSkuAttrValueList(), skuInfo.getId());
        skuInfo.setSkuAttrValueList(skuAttrValues);
        //保存sku销售属性表的信息
        List<SkuSaleAttrValue> skuSaleAttrValues =
                saveSkuSaleAttrValue(skuInfo.getSkuSaleAttrValueList(),
                        skuInfo.getId(),
                        skuInfo.getSpuId());
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValues);
        //返回结果
        return skuInfo;
    }


    /**
     * 保存sku的销售属性值的信息
     * @param skuSaleAttrValueList
     * @param id
     * @param spuId
     */
    private List<SkuSaleAttrValue> saveSkuSaleAttrValue(List<SkuSaleAttrValue> skuSaleAttrValueList,
                                                        Long id,
                                                        Long spuId) {
        return skuSaleAttrValueList.stream().map(skuSaleAttrValue -> {
            //补全skuid
            skuSaleAttrValue.setSkuId(id);
            //补全spuid
            skuSaleAttrValue.setSpuId(spuId);
            //保存数据
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            //返回结果
            return skuSaleAttrValue;
        }).collect(Collectors.toList());
    }



    /**
     * 保存sku的平台属性信息
     * @param skuAttrValueList
     * @param id
     */
    private List<SkuAttrValue> saveSkuAttrInfo(List<SkuAttrValue> skuAttrValueList, Long id) {
        return skuAttrValueList.stream().map(
                skuAttrValue -> {
                    //补全skuid
                    skuAttrValue.setSkuId(id);
                    //保存数据
                    skuAttrValueMapper.insert(skuAttrValue);
                    //返回
                    return skuAttrValue;
                }
        ).collect(Collectors.toList());
    }


    /**
     * 保存sku的图片信息
     * @param skuImageList
     * @param id
     */
    private List<SkuImage> saveSkuImage(List<SkuImage> skuImageList, Long id) {
       return skuImageList.stream().map(skuImage -> {
            //补全sku的id
            skuImage.setSkuId(id);
            //保存数据
            skuImageMapper.insert(skuImage);
            //返回
            return skuImage;
        }).collect(Collectors.toList());
    }


    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ListFeign listFeign;
    /**
     * 数据库中:商品上架或下架
     *
     * @param skuId
     * @param status
     * @return
     */
    @Override
    public SkuInfo onSaleOrOff(Long skuId, Short status) {
        //判断参数
        if(skuId == null){
            throw new RuntimeException("操作失败,参数错误!");
        }
        //查询sku的信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo == null || skuInfo.getId() == null){
            throw new RuntimeException("商品不存在!");
        }
        skuInfo.setIsSale(status.intValue());
        skuInfoMapper.updateById(skuInfo);
//        //执行操作:如果是上架,需要同步es---------------同步调用--待优化
//        if(status.equals(ProductConst.SKU_ON_SALE)){
//            listFeign.upper(skuId);
//        }else{
//            //如果是下架,需要es中删除数据
//            listFeign.down(skuId);
//        }
        //上架或下架则,发送一条消息
        if(status.equals(ProductConst.SKU_ON_SALE)){
            rabbitTemplate.convertAndSend("sku_up_or_down_exchange", "sku.up" , skuId + "");
        }else{
            rabbitTemplate.convertAndSend("sku_up_or_down_exchange", "sku.down" , skuId + "");
        }
        return skuInfo;
    }
}


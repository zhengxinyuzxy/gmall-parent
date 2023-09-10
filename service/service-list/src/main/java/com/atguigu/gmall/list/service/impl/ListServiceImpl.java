package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private GoodsDao goodsDao;
    /**
     * es中商品上架
     *
     * @param skuId
     */
    @Override
    public void goodsUpper(Long skuId) {
        //参数校验
        if(skuId == null){
            return;
        }
        //查询sku的信息,并将skuinfo对象转换为Goods对象
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        if(skuInfo == null || skuInfo.getId() == null){
            return;
        }
        //es对象进行初始化
        Goods goods = new Goods();
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setCreateTime(new Date());
        //品牌的信息设置
        BaseTrademark baseTrademark =
                productFeign.getBaseTrademark(skuInfo.getTmId());
        if(baseTrademark == null || baseTrademark.getId() == null){
            return;
        }
        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        //查询分类的信息
        BaseCategoryView baseCategoryView =
                productFeign.getBaseCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(baseCategoryView.getCategory1Id());
        goods.setCategory1Name(baseCategoryView.getCategory1Name());
        goods.setCategory2Id(baseCategoryView.getCategory2Id());
        goods.setCategory2Name(baseCategoryView.getCategory2Name());
        goods.setCategory3Id(baseCategoryView.getCategory3Id());
        goods.setCategory3Name(baseCategoryView.getCategory3Name());
        //设置平台属性
        List<BaseAttrInfo> baseAttrInfoList =
                productFeign.getSkuBaseAttrInfoBySkuId(skuInfo.getId());
        if(baseAttrInfoList.isEmpty()){
            return;
        }
        //将baseAttrInfoList转换为List<SearchAttr>
        List<SearchAttr> searchAttrs = baseAttrInfoList.stream().map(baseAttrInfo -> {
            //初始化
            SearchAttr searchAttr = new SearchAttr();
            //设置属性值
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            //返回
            return searchAttr;
        }).collect(Collectors.toList());
        //存入goods
        goods.setAttrs(searchAttrs);
        //将数据写入到es中去
        goodsDao.save(goods);
    }

    /**
     * es中商品下架
     *
     * @param skuId
     */
    @Override
    public void goodsDown(Long skuId) {
        //参数校验
        if(skuId == null){
            return;
        }
        //删除数据
        goodsDao.deleteById(skuId);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 增加热度值
     *
     * @param skuId
     */
    @Override
    public void addScore(Long skuId) {
        //参数校验
        if(skuId == null){
            return;
        }
        //查询商品是否在es中存在
        Goods goods = goodsDao.findById(skuId).get();
        if(goods == null || goods.getId() == null){
            return;
        }
        //value:key= string  value= String/Number
        //hash:key= string  value= HashMap<String,String>
        //set:key= string  value= Set<String>
        //set:key= string  value= ZSet<String>----score
        //redis自增,0  i++
        Double score = redisTemplate.opsForZSet().incrementScore("hotGoods", "goods:" + skuId, 1);
        //判断是否为10的整数倍
        if(score % 10 == 0){
            //修改热度值
            goods.setHotScore(score.longValue());
            //更新数据
            goodsDao.save(goods);
        }
    }
}

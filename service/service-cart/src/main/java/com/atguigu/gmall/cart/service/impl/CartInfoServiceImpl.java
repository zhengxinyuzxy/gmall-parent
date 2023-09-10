package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.util.GmallThreadLocalUtils;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.feign.ProductFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.AtomicDouble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Resource
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private ProductFeign productFeign;

    /**
     * 新增购物车
     *
     * @param skuId
     * @param num
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addCartInfo(Long skuId, Integer num) {
        //参数校验
        if(skuId == null || num == null){
            return;
        }
        String username = GmallThreadLocalUtils.getUserName();
        //查询购物车的数据
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getSkuId, skuId));
        //这个商品已经在购物车的场合
        if(cartInfo != null && cartInfo.getId() != null){
            //修改购物车的数据
            num = cartInfo.getSkuNum() + num;
            if(num <= 0){
                //删除购物车数据
                cartInfoMapper.deleteById(cartInfo.getId());
                return;
            }else{
                cartInfo.setSkuNum(num);
                cartInfoMapper.updateById(cartInfo);
                return;
            }
        }
        //准备新增
        if(num <= 0){
            return;
        }
        //查询商品的信息
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        if(skuInfo == null || skuInfo.getId() == null){
            return;
        }
        //补全cartinfo对象的信息
        cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(username);
        //查询sku的价格
        BigDecimal price = productFeign.getPrice(skuId);
        cartInfo.setCartPrice(price);
        cartInfo.setSkuNum(num);
        cartInfo.setIsChecked(1);
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        //保存购物车信息
        cartInfoMapper.insert(cartInfo);
    }

    /**
     * 查询购物车数据
     *
     * @return
     */
    @Override
    public List<CartInfo> getCartInfo() {
        String username = GmallThreadLocalUtils.getUserName();
        //从redis中获取数据
        List<CartInfo> cartInfoList =
                (List<CartInfo>)redisTemplate.opsForValue().get(username);
        //判断
        if(cartInfoList ==null || cartInfoList.isEmpty()){
            cartInfoList = cartInfoMapper.selectList(
                    new LambdaQueryWrapper<CartInfo>()
                            .eq(CartInfo::getUserId, username));
            //将数据存入redis
            redisTemplate.opsForValue().set(username, cartInfoList);
        }
        //reids有数据返回
        return cartInfoList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 删除购物车
     *
     * @param id
     * @return
     */
    @Override
    public List<CartInfo> del(Long id) {
        String username = GmallThreadLocalUtils.getUserName();
        //删除redis数据
        redisTemplate.delete(username);
        //删除数据库数据
        cartInfoMapper.deleteById(id);
        //重新查询数据
        return getCartInfo();
    }

    /**
     * 修改选中状态
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public void changeChecked(Long id, Integer status) {
        String username = GmallThreadLocalUtils.getUserName();
        //删除redis数据
        redisTemplate.delete(username);
        if(id != null){
            //修改一条数据的状态
            CartInfo cartInfo = cartInfoMapper.selectById(id);
            if(cartInfo == null || cartInfo.getId() == null){
                return;
            }
            //修改选中的状态
            cartInfo.setIsChecked(status);
            //修改
            cartInfoMapper.updateById(cartInfo);
        }
        //全选/全部取消的情况
        cartInfoMapper.updateCartInfo(status, username);
    }

    /**
     * 合并购物车
     *
     * @param cartInfos
     */
    @Override
    public void mergeCartInfo(List<CartInfo> cartInfos) {
        for (CartInfo cartInfo : cartInfos) {
            //新增购车
            addCartInfo(cartInfo.getSkuId(), cartInfo.getSkuNum());
        }
    }

    /**
     * 查询订单确认页的购物车信息
     *
     * @return
     */
    @Override
    public Map<String, Object> getOrderConfirm() {
        Map<String, Object> result = new HashMap<>();
        //获取用户名
        String userName = GmallThreadLocalUtils.getUserName();
        //数据库中查询选中的购物车数据
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, userName)
                        .eq(CartInfo::getIsChecked, 1));
        //判断是否为空
        if(cartInfoList == null || cartInfoList.isEmpty()){
            return null;
        }

        //计算总金额,计算总数量
        AtomicDouble total = new AtomicDouble(0);
        AtomicInteger totalNum = new AtomicInteger(0);
        //方案一for循环
//        for (CartInfo cartInfo : cartInfoList) {
//            //商品的数量
//            Integer skuNum = cartInfo.getSkuNum();
//            totalNum += skuNum;
//            //商品的实时单价
//            BigDecimal price = productFeign.getPrice(cartInfo.getSkuId());
//            //商品的总价
//            total += skuNum * price.doubleValue();
//        }
        //方案二,数据流
        List<CartInfo> collect = cartInfoList.stream().map(cartInfo -> {
            //商品的数量
            Integer skuNum = cartInfo.getSkuNum();
            totalNum.addAndGet(skuNum);
            //商品的实时单价
            BigDecimal price = productFeign.getPrice(cartInfo.getSkuId());
            //商品的总价
            total.addAndGet(skuNum * price.doubleValue());
            //保存实时价格
            cartInfo.setSkuPrice(price);
            //处理结束
            return cartInfo;
        }).collect(Collectors.toList());

        result.put("total",total);
        result.put("totalNum",totalNum);
        result.put("cartInfoList", JSONObject.toJSONString(collect));
        //返回结果
        return result;
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @Override
    public Boolean delCartInfo() {
        //获取用户名
        String userName = GmallThreadLocalUtils.getUserName();
        //删除数据库中购物车的数据
        int delete = cartInfoMapper.delete(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, userName)
                        .eq(CartInfo::getIsChecked, 1));
        return delete>0?true:false;
    }
}

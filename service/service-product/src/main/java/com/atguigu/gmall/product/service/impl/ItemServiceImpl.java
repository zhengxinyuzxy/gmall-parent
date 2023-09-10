package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class ItemServiceImpl implements ItemService {

    @Resource
    private SkuInfoMapper skuInfoMapper;
    /**
     * 根据id查询sku的详细信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return skuInfoMapper.selectById(skuId);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据id查询sku的详细信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoFromRedisOrDb(Long skuId) {
        //参数校验
        if(skuId == null){
            return null;
        }
        String key = "sku:" + skuId + ":info";
        //从redis中获取数据 key = sku:1:info--魔法值
        SkuInfo skuInfo = (SkuInfo)redisTemplate.opsForValue().get(key);
        //若redis中有数据,则直接返回
        if(skuInfo != null){
            return skuInfo;
        }
        //锁的value
        String uuid = UUID.randomUUID().toString();
        //锁的key
        String lockKey = "sku:" + skuId + ":lock";
        //加锁
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 200, TimeUnit.SECONDS);
        if(aBoolean){
            //没有数据,从数据库中查询数据
            skuInfo = getSkuInfo(skuId);
            //并且将数据库中的数据写入redis中去
            if(skuInfo == null || skuInfo.getId() == null){
                //依然写入缓存,防止击穿
                skuInfo = new SkuInfo();
                redisTemplate.opsForValue().set(key, skuInfo, 300, TimeUnit.SECONDS);
            }else{
                redisTemplate.opsForValue().set(key, skuInfo);
            }
            //释放锁
            DefaultRedisScript<Long> script = new DefaultRedisScript();
            script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
            script.setResultType(Long.class);
            //执行脚本,释放锁
            redisTemplate.execute(script, Arrays.asList(lockKey), uuid);
            //返回结果
            return skuInfo;
        }else{
            try {
                Thread.sleep(100);
                //加锁失败
                return getSkuInfoFromRedisOrDb(skuId);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    @Autowired
    private RedissonClient redissonClient;
    /**
     * 根据id查询sku的详细信息redis集群方案
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoFromRedission(Long skuId) {
        //参数校验
        if(skuId == null){
            return null;
        }
        //查缓存
        String key = "sku:" + skuId + ":info";
        //从redis中获取数据 key = sku:1:info--魔法值
        SkuInfo skuInfo = (SkuInfo)redisTemplate.opsForValue().get(key);
        //若redis中有数据,则直接返回
        if(skuInfo != null){
            return skuInfo;
        }
        //锁的key
        String lockKey = "sku:" + skuId + ":lock";
        //获取锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(100, 100, TimeUnit.SECONDS)){
                //没有数据,从数据库中查询数据
                skuInfo = getSkuInfo(skuId);
                //并且将数据库中的数据写入redis中去
                if(skuInfo == null || skuInfo.getId() == null){
                    //依然写入缓存,防止击穿
                    skuInfo = new SkuInfo();
                    redisTemplate.opsForValue().set(key, skuInfo, 300, TimeUnit.SECONDS);
                }else{
                    redisTemplate.opsForValue().set(key, skuInfo);
                }
                //返回结果
                return skuInfo;
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            //释放锁
            lock.unlock();
        }
    }

    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;
    /**
     * 根据三级分类查询一级二级三级分类的详细信息
     *
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getBaseCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Resource
    private SkuImageMapper skuImageMapper;
    /**
     * 根据sku的id查询该sku的所有图片
     *
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getSkuImageList(Long skuId) {
        return skuImageMapper.selectList(
                new LambdaQueryWrapper<SkuImage>()
                        .eq(SkuImage::getSkuId, skuId));
    }

    /**
     * 查询sku的价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    /**
     * 根据spu和sku的id查询所有的销售属性和销售属性值,并且标注出当前的sku是哪个
     *
     * @param spuId
     * @param skuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(Long spuId, Long skuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrBySpuIdAndSkuId(spuId, skuId);
    }

    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    /**
     * 根据spu的id查询该spu下所有sku的销售属性的键值对
     *
     * @param spuId
     * @return
     */
    @Override
    public Map getSkuSaleValueKeys(Long spuId) {
        //返回结果进行初始化
        Map<String, Object>  map = new HashMap<>();
        List<Map> maps = skuSaleAttrValueMapper.selectSkuSaleValueKeys(spuId);
        for (Map values : maps) {
            //获取sku的id
            Object skuId = values.get("sku_id");
            //获取值的字符串
            String valueIds = values.get("value_ids").toString();
            //保存
            map.put(valueIds, skuId);
        }
        //返回结果
        return map;
    }

    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;
    /**
     * 查询品牌的详情
     *
     * @param id
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademark(Long id) {
        return baseTradeMarkMapper.selectById(id);
    }


    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 根据sku的id查询销售属性列表
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getSkuBaseAttrInfoBySkuId(Long skuId) {
        return baseAttrInfoMapper.selectSkuBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 扣减库存
     *
     * @param map
     */
    @Override
    public Boolean decountStock(Map<String, String> map) {
        //参数校验
        if(map == null || map.size() == 0){
            return false;
        }
        //循环遍历map,循环扣减库存
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //商品id
            String key = entry.getKey();
            //扣减的数量
            String value = entry.getValue();
            skuInfoMapper.decountStock(Long.parseLong(key), Integer.parseInt(value));
//            //查询商品的信息
//            SkuInfo skuInfo = skuInfoMapper.selectById(key);
//            if(skuInfo == null || skuInfo.getId() == null){
//                throw new RuntimeException(key + "商品的库存不足!商品已经不存在了!");
//            }
//            //扣减库存
//            Integer stock = skuInfo.getStock() - Integer.parseInt(value);
//            if(stock < 0){
//                throw new RuntimeException("商品的库存不足!");
//            }
//            skuInfo.setStock(stock);
//            //更新数据
//            skuInfoMapper.updateById(skuInfo);
        }
        return true;
    }

    /**
     * 回滚库存
     *
     * @param map
     */
    @Override
    public Boolean rollbackStock(Map<String, String> map) {
        //参数校验
        if(map == null || map.size() == 0){
            return false;
        }
        //循环遍历map,循环扣减库存
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //商品id
            String key = entry.getKey();
            //扣减的数量
            String value = entry.getValue();
            //回滚库存
            skuInfoMapper.rollbackStock(Long.parseLong(key), Integer.parseInt(value));
        }
        return true;
    }
}

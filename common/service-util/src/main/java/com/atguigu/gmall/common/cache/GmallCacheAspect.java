package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    /**
     * 增强方法
     * @param point
     * @return
     */
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point){

        /*
        1.  获取参数列表
        2.  获取方法上的注解
        3.  获取前缀
        4.  获取目标方法的返回值
         */
        Object result = null;
        try {
            //获取方法的请求参数
            Object[] args = point.getArgs();
            //获取签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            //获取方法的修辞的注解
            GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
            // 前缀: sku:
            String prefix = gmallCache.prefix();
            // 从缓存中获取数据 sku:[1]
            String key = prefix+Arrays.asList(args).toString();
            // 获取缓存数据
            result = cacheHit(signature, key);
            //判断缓存中有没有数据
            if (result != null){
                // 缓存有数据
                return result;
            }
            // 初始化分布式锁sku:[1]:lock
            String lockKey = key + ":lock";
            RLock lock = redissonClient.getLock(lockKey);
            boolean flag = lock.tryLock(100, 100, TimeUnit.SECONDS);
            if (flag){
               try {
                   try {
                       //执行方法
                       result = point.proceed(point.getArgs());
                       // 防止缓存穿透
                       if (null == result){
                           // 并把结果放入缓存,空值的有效期为300s
                           Object o = new Object();
                           this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(o), 300, TimeUnit.SECONDS);
                           return o;
                       }
                   } catch (Throwable throwable) {
                       throwable.printStackTrace();
                   }
                   // 并把结果放入缓存
                   this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result));
                   return result;
               }catch (Exception e){
                   e.printStackTrace();
               }finally {
                   // 释放锁
                   lock.unlock();
               }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //boolean flag = lock.tryLock(10L, 10L, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 从缓存中获取数据
     * @param signature
     * @param key: sku:[1]
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        // 1. 查询缓存
        String cache = (String)redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cache)) {
            // 有，则反序列化，直接返回SkuInfo.class
            Class returnType = signature.getReturnType(); // 获取方法返回类型
            // 不能使用parseArray<cache, T>，因为不知道List<T>中的泛型
            return JSONObject.parseObject(cache, returnType);
        }
        return null;
    }

}

package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void setValue() {
        //随机数生成
        String uuid = UUID.randomUUID().toString();
        //尝试加锁
        Boolean lockKey = redisTemplate.opsForValue().setIfAbsent("lockKey", uuid, 3, TimeUnit.SECONDS);
        //成功则进行abc++操作,否则重试
        if(lockKey){
            //从redis中获取一个key=abc  get abc回车
            Integer abc = (Integer)redisTemplate.opsForValue().get("abc");
            //若abc不为空,那么对abc的值进行+1操作
            if(abc == null){
                return;
            }
            abc++;
            //将值更新回redis中去  setnx abc 1
            redisTemplate.opsForValue().set("abc", abc);
            int i = 1/1;
            //释放锁:获取值并且判断是不是自己的锁,并且是的话删除,不是不管---lua表达式,lua脚本--->abc.lua
            //声明脚本对象
            DefaultRedisScript<Long> script = new DefaultRedisScript();
            script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
            script.setResultType(Long.class);
            //执行脚本,释放锁
            redisTemplate.execute(script, Arrays.asList("lockKey"), uuid);
//            String lockKey1 = (String)redisTemplate.opsForValue().get("lockKey");
//            if(uuid.equals(lockKey1)){
//                //释放锁
//                redisTemplate.delete("lockKey");
//            }else{
//                //锁已经不是自己的了
//                return;
//            }

        }else{
            try {
                Thread.sleep(1000);
                setValue();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Autowired
    private RedissonClient redissonClient;
    @Override
    public void setValueRedission() {
        //获取锁
        RLock lock = redissonClient.getLock("lockKey");
        try {
            //加锁
            if(lock.tryLock(100,100, TimeUnit.SECONDS)){
                //从redis中获取一个key=abc  get abc回车
                Integer abc = (Integer)redisTemplate.opsForValue().get("abc");
                //若abc不为空,那么对abc的值进行+1操作
                if(abc == null){
                    return;
                }
                abc++;
                //将值更新回redis中去  setnx abc 1
                redisTemplate.opsForValue().set("abc", abc);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }





    }
}

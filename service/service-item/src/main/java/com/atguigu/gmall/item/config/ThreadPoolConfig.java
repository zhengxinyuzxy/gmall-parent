package com.atguigu.gmall.item.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        /**
         * 核心线程数
         * 拥有最多线程数
         * 表示空闲线程的存活时间:非核心线程
         * 存活时间单位
         * 用于缓存任务的阻塞队列
         * 省略：
         *  threadFactory：指定创建线程的工厂
         *  handler：表示当workQueue已满，且池中的线程数达到maximumPoolSize时，线程池拒绝添加新任务时采取的策略。
         */
        return new ThreadPoolExecutor(50,
                500,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000));
    }

    /**
     * 线程池的膨胀流程:
     * 1.线程池初始化完成,初始完成以后的线程池中的线程数量为:0个
     * 2.当来了一个任务以后,进行判断,线程池中是否有空闲的核心线程,若没有核心线程或者核心线程数小于定义的核心线程数,则创建核心线程
     * 3.当任务数量为50个的时候,.核心线程数扩充到50个以后,50个核心线程都在工作
     * 4.来了第51个任务,第51个任务进入阻塞队列,直到任务将阻塞队列填满,又来了10000个任务,任务总数为:10050个
     * 5.来了450个任务,创建非核心线程共450个,此时线程池满啦,线程数量为:50个核心+450个非核心=500个
     * 6.再来任何任务都会触发拒绝策略:默认的拒绝策略为:任务不执行,抛出异常
     */
}
package com.atguigu.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


/**
 * 商品管理微服务的启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.atguigu.gmall")//指定包扫描,开启自己指定的功能
@MapperScan("com.atguigu.gmall.product.mapper")
@EnableFeignClients("com.atguigu.gmall.list.feign")
public class ProductApplication {

    /**
     * springboot启动类的工作原理二: 自动装配/自动配置
     * 1.构建spring的ioc的容器
     * 2.加载SpringBootApplication注解
     * 3.ComponentScan包扫描: 启动类所在的包下的所有类的注解以及启动类所在包子包中所有类的所有注解--自定义的bean交给容器管理
     * 4.SpringBootConfiguration: 将启动类标识为一个配置类
     * 5.EnableAutoConfiguration自动装配: 加载所有引入了坐标的对象完成对象的初始化
     * @param args:启动参数,JVM参数-Xxm
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}

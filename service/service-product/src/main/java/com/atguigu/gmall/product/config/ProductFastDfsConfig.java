package com.atguigu.gmall.product.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 商品管理微服务开启文件上传功能
 */
@Configuration
@ComponentScan("com.atguigu.gmall.common.fastdfs")
public class ProductFastDfsConfig {
}

package com.atguigu.gmall.product.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductRabbitConfig {

    /**
     * 商品上下架的交换机
     * @return
     */
    @Bean("myExchange")
    public Exchange myExchange(){
        return ExchangeBuilder.directExchange("sku_up_or_down_exchange").build();
    }

    /**
     * 商品上下架的队列
     * @return
     */
    @Bean("myQueue1")
    public Queue myQueue1(){
        return QueueBuilder.durable("sku_up_queue").build();
    }

    /**
     * 商品上下架的队列
     * @return
     */
    @Bean("myQueue2")
    public Queue myQueue2(){
        return QueueBuilder.durable("sku_down_queue").build();
    }

    /**
     * 上架队列绑定
     * @param myExchange
     * @param myQueue1
     * @return
     */
    @Bean
    public Binding myBinding1(@Qualifier("myExchange") Exchange myExchange,
                             @Qualifier("myQueue1") Queue myQueue1){
        return BindingBuilder.bind(myQueue1).to(myExchange).with("sku.up").noargs();

    }

    /**
     * 下架队列绑定
     * @param myExchange
     * @param myQueue2
     * @return
     */
    @Bean
    public Binding myBinding2(@Qualifier("myExchange") Exchange myExchange,
                             @Qualifier("myQueue2") Queue myQueue2){
        return BindingBuilder.bind(myQueue2).to(myExchange).with("sku.down").noargs();

    }
}

package com.atguigu.gmall.activity.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeckillRabbitConfig {

    /**
     * 秒杀下单交换机
     * @return
     */
    @Bean("myExchange")
    public Exchange myExchange(){
        return ExchangeBuilder.directExchange("seckill_order_exchange").build();
    }

    /**
     * 秒杀下单队列
     * @return
     */
    @Bean("myQueue1")
    public Queue myQueue1(){
        return QueueBuilder.durable("seckill_order_queue").build();
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
        return BindingBuilder.bind(myQueue1).to(myExchange).with("seckill.order.add").noargs();

    }

}

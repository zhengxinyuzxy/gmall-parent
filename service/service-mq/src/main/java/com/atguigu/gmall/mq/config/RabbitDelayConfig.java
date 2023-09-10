package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq的构建队列 交换机 绑定的配置类
 */
@Configuration
public class RabbitDelayConfig {

    /**
     * 正常交换机
     */
    @Bean("nomalExchange")
    public Exchange nomal(){
        return ExchangeBuilder.topicExchange("nomal_exchange").build();
    }

    /**
     * 死信交换机
     */
    @Bean("deadExchange")
    public Exchange dead(){
        return ExchangeBuilder.topicExchange("dead_exchange").build();
    }

    /**
     * 创建队列
     */
    @Bean("nomalQueue")
    public Queue nomalQueue(){
        return QueueBuilder.durable("nomal_queue").build();
    }

    /**
     * 创建队列
     */
    @Bean("deadQueue")
    public Queue deadQueue(){
        return QueueBuilder
                .durable("dead_queue")
                .withArgument("x-dead-letter-exchange","dead_exchange")
                .withArgument("x-dead-letter-routing-key","nomal.a")
                .build();
    }

    /**
     * 正常交换机和死信队列的绑定
     * @param dead
     * @param nomalQueue
     * @return
     */
    @Bean
    public Binding deadBinding(@Qualifier("deadExchange") Exchange dead,
                                @Qualifier("nomalQueue") Queue nomalQueue){
        return BindingBuilder.bind(nomalQueue).to(dead).with("nomal.#").noargs();
    }

    /**
     * 正常交换机和死信队列的绑定
     * @param nomal
     * @param deadQueue
     * @return
     */
    @Bean
    public Binding nomalBinding(@Qualifier("nomalExchange") Exchange nomal,
                                @Qualifier("deadQueue") Queue deadQueue){
        return BindingBuilder.bind(deadQueue).to(nomal).with("dead.#").noargs();
    }

}

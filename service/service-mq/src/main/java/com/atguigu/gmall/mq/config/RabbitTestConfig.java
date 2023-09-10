package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq的构建队列 交换机 绑定的配置类
 */
@Configuration
public class RabbitTestConfig {

    /**
     * 创建交换机
     */
    @Bean("myExchange")
    public Exchange myExchange(){
//        return new TopicExchange("exchange_java0422");
        return ExchangeBuilder.topicExchange("exchange_java0422").build();
    }

    /**
     * 创建队列
     */
    @Bean("myQueue")
    public Queue myQueue(){
//        return new Queue("queue_java0422", true);
        return QueueBuilder.durable("queue_java0422_2").build();
    }

    /**
     * 创建队列1
     */
    @Bean("myQueue1")
    public Queue myQueue1(){
//        return new Queue("queue_java0422", true);
        return QueueBuilder.durable("queue_java0422_1").build();
    }

    /**
     * 绑定交换机和队列
     */
    @Bean
    public Binding myBinding(@Qualifier("myExchange") Exchange myExchange,
                             @Qualifier("myQueue") Queue myQueue){
        return BindingBuilder.bind(myQueue).to(myExchange).with("user.#").noargs();
    }

    /**
     * 绑定交换机和队列
     */
    @Bean
    public Binding myBinding1(@Qualifier("myExchange") Exchange myExchange,
                             @Qualifier("myQueue1") Queue myQueue1){
        return BindingBuilder.bind(myQueue1).to(myExchange).with("java.#").noargs();
    }
}

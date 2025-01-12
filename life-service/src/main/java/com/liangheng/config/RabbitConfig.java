package com.liangheng.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue blogQueue() {
        return new Queue("blog.queue", false);
    }

    @Bean
    public DirectExchange blogExchange() {
        return new DirectExchange("blog.exchange");
    }

    @Bean
    public Binding binding(Queue blogQueue, DirectExchange blogExchange) {
        return BindingBuilder.bind(blogQueue).to(blogExchange).with("blog.event");
    }

}




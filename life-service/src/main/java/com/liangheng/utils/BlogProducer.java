package com.liangheng.utils;

import com.liangheng.dto.BlogEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BlogProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendBlogEvent(BlogEvent event) {
        rabbitTemplate.convertAndSend("blog.exchange", "blog.event", event);
    }
}



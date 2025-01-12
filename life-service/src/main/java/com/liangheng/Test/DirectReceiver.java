package com.liangheng.Test;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "TestDirectQueue")
public class DirectReceiver {

    @RabbitHandler
    public void process(String s){
        System.out.println(s);

    }
}

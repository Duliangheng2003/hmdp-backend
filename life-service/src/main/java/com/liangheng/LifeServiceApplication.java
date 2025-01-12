package com.liangheng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.liangheng.mapper")
@SpringBootApplication
public class LifeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeServiceApplication.class, args);
    }

}

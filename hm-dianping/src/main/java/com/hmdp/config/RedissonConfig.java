package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        //配置类
        Config config = new Config();
        //添加redis单点地址
        config.useSingleServer().setAddress("redis://192.168.202.131:6379").setPassword("1234");
        //创建RedissonClient对象
        return Redisson.create(config);
    }

}

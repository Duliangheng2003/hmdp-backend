package com.liangheng.utils;

import com.liangheng.dto.BlogEvent;
import com.liangheng.entity.Follow;
import com.liangheng.service.IFollowService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.liangheng.utils.RedisConstants.FEED_KEY;

@Component
public class BlogConsumer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @RabbitListener(queues = "blog.queue")
    public void handleBlogEvent(BlogEvent event) {
        // 查询所有关注该发布者的用户
        List<Follow> follows = followService.query()
                .eq("follow_user_id", event.getUserId()).list();
        // 推送帖子id给所有关注用户
        for (Follow follow : follows) {
            Long userId = follow.getUserId();
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet()
                    .add(key, event.getBlogId().toString(), event.getTimestamp());
        }
    }
}


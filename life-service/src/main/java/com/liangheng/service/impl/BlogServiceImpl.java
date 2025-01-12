package com.liangheng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liangheng.dto.Result;
import com.liangheng.dto.ScrollResult;
import com.liangheng.dto.UserDTO;
import com.liangheng.entity.Blog;
import com.liangheng.entity.Follow;
import com.liangheng.entity.User;
import com.liangheng.mapper.BlogMapper;
import com.liangheng.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liangheng.service.IFollowService;
import com.liangheng.service.IUserService;
import com.liangheng.utils.BlogProducer;
import com.liangheng.utils.SystemConstants;
import com.liangheng.utils.UserHolder;
//import com.sun.org.apache.xpath.internal.objects.XString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.liangheng.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.liangheng.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private BlogProducer blogProducer;

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    @Override
    public Result queryBlogById(Long id) {
        // 查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("笔记不存在！");
        }
        // 查询blog有关的用户
        queryBlogUser(blog);
        // 查询blog是否被点赞
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    private void isBlogLiked(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null){
            // 用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();
        // 判断当前用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    @Override
    public Result likeBlog(Long id) {
        // 获取当前登录用户的id
        Long userId = UserHolder.getUser().getId();
        // 判断当前登录用户是否点赞
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null){
            // 如果未点赞，进行点赞操作
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 保存用户到redis的点赞集合
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        }else {
            // 如果已点赞，进行取消点赞操作
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 把用户从redis的点赞集合中移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 查询最新点赞的5位用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        // 根据用户id查询用户
        List<UserDTO> userDTOS = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        // 返回查询结果
        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(Blog blog) {
        //1.获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        //2.保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess){
            return Result.fail("新增笔记失败!");
        }
        //3.查询笔记作者的所有粉丝 select * from follow where follow_user_id = ?
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        //4.推送笔记id给所有粉丝
        for (Follow follow : follows) {
            //4.1获取粉丝id
            Long userId = follow.getUserId();
            //4.2推送
            // TODO 改为消息队列

            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        //5.返回id
        return Result.ok(blog.getId());
    }

//    @Override
//    public Result saveBlog(Blog blog) {
//        // 获取登录用户
//        UserDTO user = UserHolder.getUser();
//        blog.setUserId(user.getId());
//        // 保存帖子到数据库
//        boolean isSuccess = save(blog);
//        if (!isSuccess){
//            return Result.fail("新增帖子失败");
//        }
//        // 发送事件到消息队列
//        BlogEvent event = new BlogEvent();
//        event.setBlogId(blog.getId());
//        event.setUserId(user.getId());
//        event.setTimestamp(System.currentTimeMillis());
//        blogProducer.sendBlogEvent(event);
//        // 返回帖子id
//        return Result.ok(blog.getId());
//    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 查询收件箱
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        // 非空判断
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }
        // 解析数据
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            // 获取id
            ids.add(Long.valueOf(tuple.getValue()));
            // 获取时间戳
            long time = tuple.getScore().longValue();
            if (time == minTime){
                os++;
            }else {
                minTime = time;
                os = 1;
            }
        }
        // 根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Blog blog : blogs) {
            // 查询blog有关的用户
            queryBlogUser(blog);
            // 查询blog是否被点赞
            isBlogLiked(blog);
        }
        // 封装并返回
        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return Result.ok(r);
    }

    private void queryBlogUser(Blog blog){
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 消息队列
     */
//    private void createBlogQueue(String key, Long blogId) throws Exception {
//        try (Channel channel = RabbitMqUtils.getChannel()) {
//            // 定义队列
//            channel.queueDeclare(key, false, false, false, null);
//            //定义交换器
//            channel.exchangeDeclare("Blogs", "fanout");
//            // 绑定交换器和队列
//            channel.queueBind(key, "Blogs", "");
//            channel.basicPublish("Blogs", key, null, blogId.toString().getBytes());
//        }
//    }
//
//    private void queueConsumer(String key) throws Exception{
//        try (Channel channel = RabbitMqUtils.getChannel()) {
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                String message = new String(delivery.getBody());
//                log.info(message);
//            };
//            CancelCallback cancelCallback = (consumerTag) -> System.out.println("消息消费被中断");
//            channel.basicConsume(key, true, deliverCallback, cancelCallback);
//        }
//    }
}

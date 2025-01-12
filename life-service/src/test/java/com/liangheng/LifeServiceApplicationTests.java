package com.liangheng;

import com.liangheng.dto.LoginFormDTO;
import com.liangheng.dto.Result;
import com.liangheng.dto.UserDTO;
import com.liangheng.entity.Blog;
import com.liangheng.entity.Shop;
import com.liangheng.entity.User;
import com.liangheng.entity.Voucher;
import com.liangheng.service.IUserService;
import com.liangheng.service.impl.*;
import com.liangheng.utils.RedisIdWorker;
import com.liangheng.utils.UserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpSession;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.liangheng.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class LifeServiceApplicationTests {
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = "+id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void loadShopData() {
        //1.查询店铺信息
        List<Shop> list = shopService.list();
        //2.把店铺按照typeId分组，typeId一致的放到一个集合
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        //3.分批完成写入Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            //3.1获取类型id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            //3.2获取同类型的店铺的集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            //3.3写入redis GEOADD key 经度 维度 member
            for (Shop shop : value) {
                //stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }

    @Resource
    private UserServiceImpl userService;

    private HttpSession session = new MockHttpSession();

    private String phone = "13801828730";

    @Test
    void testLogin() {
        // 构建登录表单
        LoginFormDTO loginFormDTO = new LoginFormDTO("13801828730", "542556", "");
        // 用户登录
        Result result = userService.login(loginFormDTO, session);
        // 打印测试用例与返回结果
        System.out.println("test data: " + loginFormDTO);
        System.out.println(result);
    }



    @Test
    void testSignCount() {
        // 存储当前用户信息
        UserDTO userDTO = new UserDTO(1010L, "duliangheng", "");
        UserHolder.saveUser(userDTO);
        // 用户签到统计
        Result result = userService.signCount();
        // 打印测试用例与返回结果
        System.out.println("test data: " + userDTO);
        System.out.println(result);
    }

    @Resource
    private ShopTypeServiceImpl shopTypeService;

    @Resource
    private BlogServiceImpl blogService;

    @Resource
    private FollowServiceImpl followService;










    @Test
    void testBlogOfFollow() {
        // 用户签到统计
        Result result = userService.signCount();
        // 打印测试用例与返回结果
        System.out.println("test data: ");
        System.out.println(result);
    }



    @Test
    void testFollow() {
        // 存储当前用户信息
        UserDTO userDTO = new UserDTO(1011L, "duliangheng", "");
        UserHolder.saveUser(userDTO);
        // 关注/取关用户
        Long testId = 1016L;
        Result result = followService.follow(testId, true);
        // 打印测试用例与返回结果
        System.out.println("test id: " + testId);
        System.out.println(result);
    }

    @Test
    void testFollowCommons() {
        // 存储当前用户信息
        UserDTO userDTO = new UserDTO(1010L, "duliangheng", "");
        UserHolder.saveUser(userDTO);
        // 查询共同关注
        Long testId = 1011L;
        Result result = followService.followCommons(testId);
        // 打印测试用例与查询结果
        System.out.println("test id: " + testId);
        System.out.println(result);
    }

}

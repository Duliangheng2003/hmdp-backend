package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    public StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        String key = "chache:shopType";
        //1.查询redis中有无数据
        String shopType = stringRedisTemplate.opsForValue().get(key);
        //2.若有，则将其以list的集合返回
        if (StrUtil.isNotBlank(shopType)){
            //3.将其转换为json的集合返回
            List<ShopType> list = JSONUtil.toList(shopType, ShopType.class);
            return Result.ok(list);
        }
        //4.若无，从数据库中拿到数据
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList == null){
            //若数据库没有，直接返回失败
            return Result.fail("数据库和redis中均没有");
        }
        //5.若拿到，将其存到redis中
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(typeList));
        //6.返回成功
        return Result.ok(typeList);
    }
}

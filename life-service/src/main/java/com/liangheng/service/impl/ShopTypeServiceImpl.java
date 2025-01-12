package com.liangheng.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.liangheng.dto.Result;
import com.liangheng.entity.ShopType;
import com.liangheng.mapper.ShopTypeMapper;
import com.liangheng.service.IShopTypeService;
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
        String key = "cache:shopType";
        // 查询redis中是否有店铺类型数据
        String shopType = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopType)){
            // 若存在数据 将其转换为列表并返回
            List<ShopType> list = JSONUtil.toList(shopType, ShopType.class);
            return Result.ok(list);
        }
        // 没有则从数据库中查询对应数据
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList == null){
            // 若数据库没有 直接返回失败
            return Result.fail("数据库和redis中均没有");
        }
        // 若存在数据 将其存到redis中
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(typeList));
        // 返回查询到的数据
        return Result.ok(typeList);
    }
}


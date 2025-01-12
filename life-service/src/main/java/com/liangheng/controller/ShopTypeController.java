package com.liangheng.controller;


import com.liangheng.dto.Result;
import com.liangheng.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 店铺类型功能模块
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    /**
     * 查询店铺类型列表
     */
    @GetMapping("list")
    public Result List() {
        return typeService.queryList();
    }
}
/*
List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
 */
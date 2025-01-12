package com.liangheng.controller;


import cn.hutool.core.bean.BeanUtil;
import com.liangheng.dto.LoginFormDTO;
import com.liangheng.dto.Result;
import com.liangheng.dto.UserDTO;
import com.liangheng.entity.User;
import com.liangheng.entity.UserInfo;
import com.liangheng.service.IUserInfoService;
import com.liangheng.service.IUserService;
import com.liangheng.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 用户功能模块
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        //发送短信验证码并保存
        return userService.sendCode(phone,session);
    }

    /**
     * 登录功能
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 实现登录功能
        return userService.login(loginForm, session);
    }

    /**
     * 登出功能
     */
    @PostMapping("/logout")
    public Result logout(){
        // 移除当前用户
        UserHolder.removeUser();
        return Result.ok();
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/me")
    public Result me(){
        //获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    /**
     * 查看用户详情
     */
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询用户详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 查询结果为空则直接返回
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * 根据id查询用户
     */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询用户信息
        User user = userService.getById(userId);
        if (user == null) {
            // 查询结果为空则直接返回
            return Result.ok();
        }
        // 封装用户信息并返回
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    /**
     * 用户签到
     */
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    /**
     * 签到统计
     */
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }


}
package com.liangheng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liangheng.dto.LoginFormDTO;
import com.liangheng.dto.Result;
import com.liangheng.entity.User;

import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();


}

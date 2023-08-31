package com.yinjunbiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinjunbiao.controller.LoginFormDTO;
import com.yinjunbiao.controller.Result;
import com.yinjunbiao.entity.User;


import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}

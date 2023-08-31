package com.yinjunbiao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinjunbiao.controller.LoginFormDTO;
import com.yinjunbiao.controller.Result;
import com.yinjunbiao.entity.User;
import com.yinjunbiao.entity.UserDTO;
import com.yinjunbiao.mapper.UserMapper;
import com.yinjunbiao.service.IUserService;
import com.yinjunbiao.util.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yinjunbiao.util.RedisConstants.LOGIN_USER_KEY;
import static com.yinjunbiao.util.SystemConstants.USER_LOGIN;
import static com.yinjunbiao.util.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        //保存验证码到session
//        session.setAttribute("code",code);

        //将验证码保存到redis中
        redisTemplate.opsForValue().set(USER_LOGIN + phone, code, 5 , TimeUnit.MINUTES);

        log.debug("验证码发送成功{}", code);

        //返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {

        //校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        //校验验证码
//        Object cacheCode = session.getAttribute("code");

        String cacheCode = redisTemplate.opsForValue().get(USER_LOGIN + phone);

        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)){
            //不一致，报错
            return Result.fail("验证码错误");
        }
        //一致，根据用户查找手机
        User user = query().eq("phone", phone).one();

        //判断用户是否存在
        if (user == null){
            //不存在，创建新用户并保存
            user = creteUserWithPhone(phone);
        }


        //保存用户信息到session中
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));

        //保存用户信息到redis中
        //随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);

        //将user对象转成HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(userDTO,new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));

        //存储
        String tokenKey = LOGIN_USER_KEY + token;
        redisTemplate.opsForHash().putAll(tokenKey, stringObjectMap);
        //设置有效期
        redisTemplate.expire(tokenKey,30,TimeUnit.MINUTES);


        //返回有效期
        return Result.ok(token);
    }

    private User creteUserWithPhone(String phone) {
        //创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //保存用户
        save(user);
        return user;
    }
}

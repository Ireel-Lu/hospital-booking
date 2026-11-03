package com.hospital.booking.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.booking.dto.LoginDTO;
import com.hospital.booking.dto.RegisterDTO;
import com.hospital.booking.entity.User;

import java.util.Map;

public interface IUserService extends IService<User> {

    /**
     * 用户登录
     * @param loginDTO 登录参数：username、pwd
     * @return Result 返回登录结果（用户信息和token）
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册参数：username、pwd、realName、phone
     * @return 注册结果
     */
    void register(RegisterDTO registerDTO);

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户的ID(通过过滤器注入）
     * @return 用户信息
     */
    Map<String,Object> getUserById(Long userId);
}

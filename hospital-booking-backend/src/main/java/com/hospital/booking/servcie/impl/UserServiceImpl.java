package com.hospital.booking.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.booking.dto.LoginDTO;
import com.hospital.booking.dto.RegisterDTO;
import com.hospital.booking.entity.User;

import com.hospital.booking.mapper.UserMapper;
import com.hospital.booking.servcie.IUserService;
import com.hospital.booking.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final JwtUtils jwtUtils;
    /**
     * 用户登录
     * @param loginDTO 登录参数：username、pwd
     * @return Result 返回登录结果（用户信息和token）
     */

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper();
        wrapper.eq(User::getUsername,loginDTO.getUsername());
        User user = getOne(wrapper);

        if(user == null){
            throw new RuntimeException("用户不存在");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPwd().getBytes());
        if(!user.getPwd().equals(encryptedPassword)){
            throw new RuntimeException("密码错误");
        }

        if(user.getStatus()!= 1){
            throw new RuntimeException("用户已被禁用");
        }

        String token = jwtUtils.generateToken(user.getId(),user.getUsername(),user.getRole());

        Map<String,Object> result = new HashMap<>();
        result.put("token",token);
        result.put("userId",user.getId());
        result.put("username",user.getUsername());
        result.put("role",user.getRole());
        result.put("realName",user.getRealName());

        return result;
    }


    /**
     * 用户注册
     * @param registerDTO 注册参数：username、pwd、realName、phone
     * @return 注册结果
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,registerDTO.getUsername());
        if(this.count(wrapper)>0){
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPwd(DigestUtils.md5DigestAsHex(registerDTO.getPwd().getBytes()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setRole("patient");
        user.setStatus(1);

        this.save(user);
    }


    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户的ID(通过过滤器注入）
     * @return 用户信息
     */
    @Override
    public Map<String,Object>  getUserById(Long userId) {
        User user = this.getById(userId);
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        Map<String,Object> result = new HashMap<>();
        result.put("id",user.getId());
        result.put("username",user.getUsername());
        result.put("realName",user.getRealName());
        result.put("phone",user.getPhone());
        result.put("email",user.getEmail());
        result.put("role",user.getRole());
        result.put("avatar",user.getAvatar());

        return result;
    }

}

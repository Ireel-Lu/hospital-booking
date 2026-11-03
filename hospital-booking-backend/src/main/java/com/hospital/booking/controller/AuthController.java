package com.hospital.booking.controller;

import com.hospital.booking.dto.LoginDTO;
import com.hospital.booking.dto.RegisterDTO;
import com.hospital.booking.entity.Result;
import com.hospital.booking.servcie.IUserService;
import com.hospital.booking.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;

    private final JwtUtils jwtUtils;

    /**
    * 用户登录
    * @param loginDTO 登录参数：username、pwd
    * @return Result 返回登录结果（用户信息和token）
    */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO loginDTO){
        Map<String,Object> result = userService.login(loginDTO);
        return Result.success(result);
    }

    /**
     * 用户注册
     * @param registerDTO 注册参数：username、pwd、realName、phone
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO registerDTO){
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    /**
     * 用户登出
     * @param token 请求头中的token
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        // 这里可以根据实际需求进行登出操作，比如清除缓存中的token等
        jwtUtils.invalidateToken(token);
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户的ID(通过过滤器注入）
     * @return 用户信息
     */
    public Result getCurrentUserInfo(@RequestAttribute Long userId){
        return Result.success(userService.getUserById(userId));
    }

}

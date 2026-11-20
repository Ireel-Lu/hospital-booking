package com.hospital.controller;

import com.hospital.dto.LoginDTO;
import com.hospital.dto.RegisterDTO;
import com.hospital.entity.Result;
import com.hospital.servcie.IUserService;
import com.hospital.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证", description = "AuthController")
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
    @Operation(summary = "用户登录（login）")
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
    @Operation(summary = "用户注册（register）")
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
    @Operation(summary = "用户登出（logout）")
    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "Authorization", required = false) String authHeader){
        //请求头格式为 "Bearer xxx"，拉黑前必须剥离前缀，否则黑名单key与校验时的token不一致
        if(authHeader != null && !authHeader.isBlank()){
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            jwtUtils.invalidateToken(token);
        }
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户的ID(通过过滤器注入）
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息（getCurrentUserInfo）")
    @GetMapping("/info")
    public Result getCurrentUserInfo(@RequestAttribute Long userId){
        return Result.success(userService.getUserById(userId));
    }

}

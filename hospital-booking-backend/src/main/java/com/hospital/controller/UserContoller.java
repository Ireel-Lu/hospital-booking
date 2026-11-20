package com.hospital.controller;

import com.hospital.entity.Result;
import com.hospital.entity.User;
import com.hospital.servcie.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理", description = "UserContoller")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserContoller {

    private final IUserService userService;

    /**
     * 查询用户列表
     * @param keyword 搜索关键字（用户名、真实姓名、手机号）
     * @return 用户列表数据
     */
    @Operation(summary = "查询用户列表（list）")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false)String keyword){
        return Result.success(userService.listUsers(keyword));
    }

    /**
     * 更新用户状态
     * @param user 用户对象，包含id、status
     * @return 更新结果
     */
    @Operation(summary = "更新用户状态（updateStatus）")
    @PutMapping("/status")
    public Result updateStatus(@RequestBody User user){
        return toResult(userService.updateUserStatus(user));
    }

    /**
     * 编辑用户信息
     * @param params 用户信息参数：id、username、realName、phone、role
     * @return 更新结果
     */
    @Operation(summary = "编辑用户信息（update）")
    @PutMapping("/update")
    public Result update(@RequestBody Map<String,Object> params){
        return toResult(userService.updateUser(params));
    }

    /**
     * 重置用户密码
     * @param params 用户信息参数：id、pwd
     * @return 更新结果
     */
    @Operation(summary = "重置用户密码（resetPwd）")
    @PutMapping("/reset-password")
    public Result resetPwd(@RequestBody Map<String,Object> params){
        return toResult(userService.resetPassword(params));
    }

    /**
     * 删除用户
     * @param id 用户id
     * @return 删除结果
     */
    @Operation(summary = "删除用户（delete）")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 将服务层返回的success/message转换为统一响应对象
     */
    private Result toResult(Map<String,Object> serviceResult){
        if(Boolean.TRUE.equals(serviceResult.get("success"))){
            return Result.success(serviceResult.get("message"));
        }
        return Result.error((String) serviceResult.get("message"));
    }

}

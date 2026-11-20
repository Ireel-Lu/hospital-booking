package com.hospital.controller;

import com.hospital.entity.Result;
import com.hospital.servcie.IUserService;
import com.hospital.servcie.OssFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 个人中心：所有登录用户都可以维护自己的资料卡
 */
@Tag(name = "个人中心", description = "ProfileController")
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserService userService;
    private final OssFileService ossFileService;

    /**
     * 获取当前登录用户的资料卡
     */
    @Operation(summary = "获取个人资料（getProfile）")
    @GetMapping
    public Result getProfile(@RequestAttribute Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 修改个人资料（用户名、性别、出生日期、地址、头像等）
     */
    @Operation(summary = "修改个人资料（updateProfile）")
    @PutMapping
    public Result updateProfile(@RequestAttribute Long userId,
                                @RequestBody Map<String, Object> params) {
        return Result.success(userService.updateProfile(userId, params));
    }

    /**
     * 修改密码（校验原密码）
     */
    @Operation(summary = "修改密码（changePassword）")
    @PutMapping("/password")
    public Result changePassword(@RequestAttribute Long userId,
                                 @RequestBody Map<String, String> params) {
        userService.changePassword(userId, params.get("oldPwd"), params.get("newPwd"));
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 上传头像，返回可访问的图片地址
     */
    @Operation(summary = "上传头像（uploadAvatar）")
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestAttribute Long userId,
                               @RequestParam("file") MultipartFile file) {
        String url = ossFileService.uploadAvatar(file, userId);
        userService.updateAvatar(userId, url);
        return Result.success(ossFileService.toSignedUrl(url));
    }

    /**
     * 医生接诊时查看患者资料卡
     */
    @Operation(summary = "查看患者资料（getPatientProfile）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/patient/{userId}")
    public Result getPatientProfile(@PathVariable Long userId) {
        return Result.success(userService.getPatientProfile(userId));
    }
}

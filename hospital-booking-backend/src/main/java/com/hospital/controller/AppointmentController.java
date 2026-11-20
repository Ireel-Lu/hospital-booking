package com.hospital.controller;

import com.hospital.annotation.Log;
import com.hospital.dto.AppointmentDTO;
import com.hospital.entity.Result;
import com.hospital.servcie.IAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "预约管理", description = "AppointmentController")
@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    /**
     * 当前登录用户是否为管理员
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    /**
     * 新增预约
     * @param appointmentDTO 预约请求参数
     * @return 统一响应对象
     */
    @Log(value="创建预约",targetType="Appointment")
    @Operation(summary = "创建预约（create）")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @PostMapping("/create")
    public Result create(@RequestBody AppointmentDTO appointmentDTO){
        Map<String,Object> result = appointmentService.createAppointment(appointmentDTO);

        if((Boolean) result.get("success")){
            return Result.success(result.get("appointment"));
        }
        return Result.error((String)result.get("message"));
    }

    /**
     * 支付预约
     * 业务逻辑：将预约状态从"待支付"更新为"已支付"
     *
     * @param id 预约ID
     * @return 统一响应对象
     */
    @Log(value = "支付预约",targetType = "Appointment")
    @Operation(summary = "支付预约（pay）")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @PutMapping("/pay/{id}")
    public Result pay(@PathVariable Long id, @RequestAttribute Long userId){
        boolean isSuccess = appointmentService.payAppointment(id, userId, isAdmin());
        return  isSuccess ? Result.success("支付成功") : Result.error("支付失败");
    }

    /**
     * 取消预约
     * 业务逻辑：
     * 1. 将预约状态更新为"已取消"
     * 2. 释放对应的号源（让其他患者可以预约）
     * 3. 记录取消原因
     *
     * @param id 预约ID
     * @param params 包含取消原因
     * @return 统一响应对象
     */
    @Log(value = "取消预约",targetType = "Appointment")
    @Operation(summary = "取消预约（cancel）")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id, @RequestBody Map<String, String> params,
                         @RequestAttribute Long userId) {
        String reason = params.get("reason");
        boolean isSuccess = appointmentService.cancelAppointment(id, reason, userId, isAdmin());
        return isSuccess ? Result.success("取消成功") : Result.error("取消失败");
    }

    /**
     * 获取当前用户的预约列表
     * 业务逻辑：
     * 1. 从请求属性中获取当前登录用户ID（由过滤器/拦截器注入，并调用服务层
     * 2. 查询该用户的所有预约记录
     * 3. 返回预约列表
     *
     * @param userId 当前登录用户ID（从请求属性获取）
     * @return 统一响应对象，包含预约列表
     */
    @Operation(summary = "我的预约（getMyAppointment）")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @GetMapping("/my")
    public Result getMyAppointment(@RequestAttribute Long userId){
        return Result.success(appointmentService.getUserAppointments(userId));
    }

    /**
     * 筛选预约列表
     * 请求参数（Query String）：
     * - departmentId: 科室ID（可选）
     * - date: 日期（可选）
     * - status: 状态（可选）
     *
     * 支持多条件组合筛选，所有参数均为可选
     * 未传递的参数不参与筛选
     *
     * @param params 筛选条件Map
     * @return 统一响应对象，包含筛选后的预约列表
     */
    @Operation(summary = "筛选预约（filterAppointments）")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/filter")
    public Result filterAppointments(@RequestParam Map<String, Object> params){
        return Result.success(appointmentService.filterAppointments(params));
    }

    /**
     * 获取预约详情
     * 路径参数：预约ID
     *
     * 返回信息包含：
     * - 预约基本信息（预约号、日期、时段、症状、状态）
     * - 患者信息（姓名、电话）
     * - 医生信息（姓名、职称）
     *
     * @param id 预约ID
     * @return 统一响应对象，包含完整的预约详情
     */
    @Operation(summary = "获取预约详情（getById）")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        Map<String,Object> detail = appointmentService.getAppointmentDetail(id);
        if(detail == null){
            return Result.error("预约不存在");
        }
        return Result.success(detail);
    }

    /**
     * 完成就诊
     * 业务逻辑：
     * 1. 医生完成诊疗后调用此接口
     * 2. 将预约状态更新为"已完成"
     * 3. 该操作通常由医生端发起
     *
     * @param id 预约ID
     * @return 统一响应对象
     */
    @Operation(summary = "完成就诊（complete）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        return appointmentService.completeAppointment(id)
                ? Result.success("就诊完成")
                : Result.error("预约不存在");
    }

}

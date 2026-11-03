package com.hospital.booking.controller;

import com.hospital.booking.annotation.Log;
import com.hospital.booking.dto.AppointmentDTO;
import com.hospital.booking.entity.Appointment;
import com.hospital.booking.entity.Doctor;
import com.hospital.booking.entity.Result;
import com.hospital.booking.entity.User;
import com.hospital.booking.mapper.DoctorMapper;
import com.hospital.booking.mapper.UserMapper;
import com.hospital.booking.servcie.IAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    private final UserMapper userMapper;

    private final DoctorMapper doctorMapper;

    /**
     * 新增预约
     * @param appointmentDTO 预约请求参数
     * @return 统一响应对象
     */
    @Log(value="创建预约",targetType="Appointment")
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
    @PutMapping("/pay/{id}")
    public Result pay(@PathVariable Long id){
        boolean isSuccess = appointmentService.payAppointment(id);
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
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        boolean isSuccess = appointmentService.cancelAppointment(id, reason);
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
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        Appointment appointment = appointmentService.getById(id);
        if(appointment == null){
            return Result.error("预约不存在");
        }

        Map<String,Object> result = new HashMap<>();

        //预约基本信息
        result.put("id", appointment.getId());
        result.put("appointmentNo", appointment.getAppointmentNo());
        result.put("appointmentDate", appointment.getAppointmentDate());
        result.put("timeSlot", appointment.getTimeSlot());
        result.put("symptoms", appointment.getSymptoms());
        result.put("status", appointment.getStatus());

        //患者信息
        User patient = userMapper.selectById(appointment.getPatientId());
        if(patient !=null){
            result.put("patientName", patient.getRealName());
            result.put("patientPhone", patient.getPhone());
        }

        //医生信息
        Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
        if(doctor != null){
            result.put("doctorName",doctor.getName());
            result.put("doctorTitle",doctor.getTitle());
        }

        return Result.success(result);
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
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        Appointment appointment = appointmentService.getById(id);
        if(appointment == null){
            return Result.error("预约不存在");
        }

        appointment.setStatus("completed");
        appointmentService.updateById(appointment);

        return Result.success("就诊完成");

    }

}

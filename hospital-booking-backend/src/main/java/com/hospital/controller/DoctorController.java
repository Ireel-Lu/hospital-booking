package com.hospital.controller;

import com.hospital.dto.SourceSettingDTO;
import com.hospital.entity.Doctor;
import com.hospital.entity.Result;
import com.hospital.servcie.IDoctorService;
import com.hospital.servcie.ISourceSettingService;
import com.hospital.servcie.OssFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "医生管理", description = "DoctorController")
@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;

    private final ISourceSettingService sourceSettingService;

    private final OssFileService ossFileService;

    /**
     * 查询医生列表
     *
     * @param departmentId 科室Id
     * @param keyword 搜索关键字
     */
    @Operation(summary = "查询医生列表（list）")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false)Long departmentId,
                       @RequestParam(required = false)String keyword){
        return Result.success(doctorService.getDoctors(departmentId,keyword));
    }

    /**
     * 根据ID查询医生详情
     *
     * @param id 医生id
     * @return 统一响应对象，包含医生详情
     */
    @Operation(summary = "根据ID查询医生详情（getById）")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        Doctor doctor = doctorService.getById(id);
        if (doctor != null) {
            doctor.setAvatar(ossFileService.toSignedUrl(doctor.getAvatar()));
        }
        return Result.success(doctor);
    }

    /**
     * 查询医生某天的排班信息
     *
     * @param doctorId 医生Id
     * @param date 日期
     * @return 统一响应对象，包含医生排班信息
     */
    @Operation(summary = "查询医生排班信息（getSchedule）")
    @GetMapping("/schedule")
    public Result getSchedule(@RequestParam Long doctorId,
                              @RequestParam String date){
        return Result.success(doctorService.getSchedule(doctorId,LocalDate.parse(date)));
    }

    /**
     * 添加医生
     *
     * @param params 医生参数Map
     * name: 姓名（必填）
     * title: 职称（必填）
     * specialty: 专长（必填）
     * departmentId: 科室ID（必填）
     * consultationFee: 挂号费（默认0）
     * introduction: 简介（可选）
     *
     * @return 统一响应对象，添加结果
     */
    @Operation(summary = "新增医生（add）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody Map<String,Object> params){
        Map<String,Object> result = doctorService.addDoctor(params);
        return Result.success(result);
    }

    /**
     * 更新医生信息
     *  @param params 更新参数Map
     *  id: 医生ID（必填）
     *  name: 姓名（可选）
     *  title: 职称（可选）
     *  specialty: 专长（可选）
     *  departmentId: 科室ID（可选）
     *  consultationFee: 挂号费（可选）
     *  introduction: 简介（可选）
     *  @return 统一响应对象，更新结果
     */
    @Operation(summary = "更新医生信息（update）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody Map<String,Object> params){
        Map<String,Object> result = doctorService.updateDoctor(params);
        if(Boolean.TRUE.equals(result.get("success"))){
            return Result.success(result.get("message"));
        }
        return Result.error((String) result.get("message"));
    }

    /**
     * 删除医生（物理删除）
     * @param id 医生ID
     * @return 统一响应对象，删除结果
     */
    @Operation(summary = "删除医生（delete）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return Result.success("删除医生成功");
    }

    /**
     * 保存医生号源设置
     * @param dto 号源设置参数
     * doctorId: 医生ID
     * date: 日期
     * timeSlot: 时间段（如 08:00-09:00）
     * totalQuantity: 总号源数
     * remainingQuantity: 剩余号源数（初始等于总号源数）
     * @return 统一响应对象
     */
    @Operation(summary = "保存号源设置（saveSourceSetting）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/source-setting/save")
    public Result saveSourceSetting(@RequestBody SourceSettingDTO dto){
        sourceSettingService.saveSetting(dto);
        return Result.success("号源设置成功");
    }

    /**
     * 查询医生某天的号源设置
     * @param doctorId 医生ID
     * @param date 日期（格式：yyyy-MM-dd）
     * @return 统一响应对象，包含号源设置列表
     */
    @Operation(summary = "查询号源设置（getSourceSetting）")
    @GetMapping("/source-setting/list")
    public Result getSourceSetting(@RequestParam Long doctorId,
                                   @RequestParam String date){
        return Result.success(sourceSettingService.getSettings(doctorId,LocalDate.parse(date)));
    }

    /**
     * 获取当前登录医生信息（医生端页面用它拿到自己的doctorId）
     * @param userId 当前登录用户ID（由JWT过滤器注入）
     * @return 医生信息
     */
    @Operation(summary = "获取当前医生信息（getCurrentDoctor）")
    @GetMapping("/current")
    public Result getCurrentDoctor(@RequestAttribute Long userId){
        return Result.success(doctorService.getByUserId(userId));
    }

}

package com.hospital.booking.controller;

import com.hospital.booking.dto.SourceSettingDTO;
import com.hospital.booking.entity.Doctor;
import com.hospital.booking.entity.Result;
import com.hospital.booking.entity.User;
import com.hospital.booking.servcie.IDoctorService;
import com.hospital.booking.servcie.ISourceSettingService;
import com.hospital.booking.servcie.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;

    private final IUserService userService;

    private final ISourceSettingService sourceSettingService;

    /** 密码加密器：Spring Security提供的BCrypt密码加密工具 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 查询医生列表
     *
     * @param departmentId 科室Id
     * @param keyword 搜索关键字
     */
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
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        return Result.success(doctorService.getById(id));
    }

    /**
     * 查询医生某天的排班信息
     *
     * @param doctorId 医生Id
     * @param date 日期
     * @return 统一响应对象，包含医生排班信息
     */
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
    @PostMapping("/add")
    public Result add(@RequestBody Map<String,Object> params){
        String name = (String) params.get("name");
        String title = (String) params.get("title");
        String specialty = (String) params.get("specialty");
        Long departmentId = params.get("departmentId") != null ?
                Long.valueOf(params.get("departmentId").toString()) : null;
        Object feeObj = params.get("consultationFee");                         // 挂号费
        java.math.BigDecimal consultationFee = feeObj != null ?
                new java.math.BigDecimal(feeObj.toString()) : java.math.BigDecimal.ZERO; // 默认0
        String introduction = (String) params.get("introduction");

        //先创建用户对象
        User user = new User();
        user.setUsername("doc"+System.currentTimeMillis()); //生成唯一用户名
        user.setPwd(passwordEncoder.encode("123456"));
        user.setRealName(name);
        user.setRole("doctor");
        user.setStatus(1); //默认启用
        userService.save(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(user.getId());
        doctor.setName(name);
        doctor.setEmployeeNo("DOC"+user.getId());
        doctor.setTitle(title);
        doctor.setSpecialty(specialty);
        doctor.setDepartmentId(departmentId);
        doctor.setConsultationFee(consultationFee);
        doctor.setIntroduction(introduction);
        doctor.setStatus(1); //默认启用
        doctorService.save(doctor);

        return Result.success("添加医生成功");

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
    @PutMapping("/update")
    public Result update(@RequestBody Map<String,Object> params){
        long id = Long.parseLong(params.get("id").toString());
        Doctor doctor = doctorService.getById(id);

        if(doctor == null){
            return Result.error("医生不存在");
        }

        //更新医生信息
        if(params.get("name") != null) doctor.setName((String) params.get("name"));
        if(params.get("title") != null) doctor.setTitle((String) params.get("title"));
        if(params.get("specialty") != null) doctor.setSpecialty((String) params.get("specialty"));
        if(params.get("departmentId") != null) doctor.setDepartmentId(Long.valueOf(params.get("departmentId").toString()));
        if(params.get("consultationFee") != null) doctor.setConsultationFee(new java.math.BigDecimal(params.get("consultationFee").toString()));
        if(params.get("introduction") != null) doctor.setIntroduction((String) params.get("introduction"));
        doctorService.updateById(doctor);

        //更新用户姓名
        User user = userService.getById(doctor.getUserId());
        if(user != null && params.get("name") != null){
            user.setRealName((String) params.get("name"));
            userService.updateById(user);
        }
        return Result.success("更新医生信息成功");
    }

    /**
     * 删除医生（物理删除）
     * @param id 医生ID
     * @return 统一响应对象，删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        Doctor doctor = doctorService.getById(id);
        if (doctor != null) {
            userService.removeById(doctor.getUserId());
            doctorService.removeById(id);
        }
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
    @GetMapping("/source-setting/list")
    public Result getSourceSetting(@RequestParam Long doctorId,
                                   @RequestParam String date){
        return Result.success(sourceSettingService.getSettings(doctorId,LocalDate.parse(date)));
    }

}

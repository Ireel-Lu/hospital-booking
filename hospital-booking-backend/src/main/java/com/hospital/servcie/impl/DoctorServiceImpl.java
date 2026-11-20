package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.servcie.IDoctorService;
import com.hospital.servcie.IUserService;
import com.hospital.servcie.OssFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements IDoctorService {

    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OssFileService ossFileService;

    @Override
    public List<Map<String,Object>> getDoctors(Long departmentId){
        return getDoctors(departmentId,null);
    }

    @Override
    public List<Map<String,Object>> getDoctors(Long departmentId, String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();

        if(departmentId != null){
            wrapper.eq(Doctor::getDepartmentId,departmentId);
        }

        if(keyword != null && !keyword.trim().isEmpty()){
            //用and包一层，避免 OR 破坏科室条件的 AND 优先级
            wrapper.and(w -> w.like(Doctor::getName,keyword)
                    .or()
                    .like(Doctor::getTitle,keyword)
                    .or()
                    .like(Doctor::getSpecialty,keyword));
        }

        wrapper.eq(Doctor::getStatus,1);

        List<Doctor> doctors = this.list(wrapper);

        return doctors.stream().map(doctor -> {
            Map<String,Object> map = new HashMap<>();
            map.put("id",doctor.getId());
            map.put("name",doctor.getName());
            map.put("title", doctor.getTitle());
            map.put("specialty", doctor.getSpecialty());
            map.put("introduction", doctor.getIntroduction());
            map.put("consultationFee", doctor.getConsultationFee());
            map.put("avatar", ossFileService.toSignedUrl(doctor.getAvatar()));
            map.put("departmentId", doctor.getDepartmentId());
            map.put("status", doctor.getStatus());

            //科室名称（科室未设置时跳过，避免空指针）
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            if(department != null){
                map.put("departmentName",department.getName());
            }

            return map;
        }).collect(Collectors.toList());

    }

    @Override
    public List<Map<String, Object>> searchDoctors(String keyword) {
        return withSignedAvatars(doctorMapper.searchDoctors(keyword));
    }

    @Override
    public List<Map<String, Object>> getAllDoctors() {
        return getDoctors(null);
    }

    @Override
    public Map<String, Object> getSchedule(Long doctorId, LocalDate date) {
        Map<String,Object> result = new HashMap<>();
        result.put("doctorId",doctorId);
        result.put("date",date.toString());

        List<Map<String,Object>> timeSlots = new ArrayList<>();
        String[] slots = {
                "08:00-09:00", "09:00-10:00", "10:00-11:00",
                "11:00-12:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"
        };

        for(String slot : slots){
            Map<String,Object> slotMap = new HashMap<>();
            slotMap.put("timeSlot",slot);
            slotMap.put("available",true);
            timeSlots.add(slotMap);
        }

        result.put("timeSlots",timeSlots);


        return result;
    }

    @Override
    public List<Map<String, Object>> getPopularDoctors(int limit) {
        return withSignedAvatars(doctorMapper.getPopularDoctors(limit));
    }

    private List<Map<String, Object>> withSignedAvatars(List<Map<String, Object>> doctors) {
        for (Map<String, Object> doctor : doctors) {
            Object avatar = doctor.get("avatar");
            if (avatar != null) {
                doctor.put("avatar", ossFileService.toSignedUrl(avatar.toString()));
            }
        }
        return doctors;
    }

    @Override
    public Doctor getByUserId(Long userId) {
        Long doctorId = doctorMapper.getDoctorIdByUserId(userId);
        Doctor doctor = doctorId == null ? null : this.getById(doctorId);
        if (doctor != null) {
            doctor.setAvatar(ossFileService.toSignedUrl(doctor.getAvatar()));
        }
        return doctor;
    }

    /**
     * 添加医生（同时创建关联的登录用户）
     * 账号可由管理员指定，未指定时自动生成；初始密码默认 123456
     */
    @Override
    @Transactional
    public Map<String, Object> addDoctor(Map<String, Object> params) {
        String name = (String) params.get("name");
        String title = (String) params.get("title");
        String specialty = (String) params.get("specialty");
        Long departmentId = params.get("departmentId") != null ?
                Long.valueOf(params.get("departmentId").toString()) : null;
        Object feeObj = params.get("consultationFee");
        BigDecimal consultationFee = feeObj != null ?
                new BigDecimal(feeObj.toString()) : BigDecimal.ZERO;
        String introduction = (String) params.get("introduction");

        //登录账号：管理员可指定，未指定则自动生成；初始密码默认 123456
        String username = params.get("username") == null ? "" : params.get("username").toString().trim();
        if (username.isEmpty()) {
            username = "doc" + System.currentTimeMillis();
        } else if (userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0) {
            throw new RuntimeException("登录账号已存在：" + username);
        }
        String initialPassword = params.get("pwd") == null || params.get("pwd").toString().isBlank()
                ? "123456" : params.get("pwd").toString().trim();

        //先创建关联的登录用户
        User user = new User();
        user.setUsername(username);
        user.setPwd(passwordEncoder.encode(initialPassword));
        user.setRealName(name);
        user.setRole("doctor");
        user.setStatus(1);//默认启用
        if (params.get("phone") != null && !params.get("phone").toString().isBlank()) {
            user.setPhone(params.get("phone").toString().trim());
        }
        if (params.get("gender") != null && !params.get("gender").toString().isBlank()) {
            user.setGender(params.get("gender").toString().startsWith("女") ? "女" : "男");
        }
        userService.save(user);

        //再创建医生记录
        Doctor doctor = new Doctor();
        doctor.setUserId(user.getId());
        doctor.setName(name);
        doctor.setEmployeeNo("DOC" + user.getId());
        doctor.setTitle(title);
        doctor.setSpecialty(specialty);
        doctor.setDepartmentId(departmentId);
        doctor.setConsultationFee(consultationFee);
        doctor.setIntroduction(introduction);
        doctor.setStatus(1);//默认启用
        this.save(doctor);

        //把账号信息返回给前端，便于管理员告知医生登录方式
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("doctorId", doctor.getId());
        result.put("username", username);
        result.put("initialPassword", initialPassword);
        result.put("message", "添加医生成功");
        return result;
    }

    /**
     * 更新医生信息（同步更新关联用户的姓名）
     */
    @Override
    @Transactional
    public Map<String, Object> updateDoctor(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        long id = Long.parseLong(params.get("id").toString());
        Doctor doctor = this.getById(id);

        if(doctor == null){
            result.put("success", false);
            result.put("message", "医生不存在");
            return result;
        }

        if(params.get("name") != null) doctor.setName((String) params.get("name"));
        if(params.get("title") != null) doctor.setTitle((String) params.get("title"));
        if(params.get("specialty") != null) doctor.setSpecialty((String) params.get("specialty"));
        if(params.get("departmentId") != null) doctor.setDepartmentId(Long.valueOf(params.get("departmentId").toString()));
        if(params.get("consultationFee") != null) doctor.setConsultationFee(new BigDecimal(params.get("consultationFee").toString()));
        if(params.get("introduction") != null) doctor.setIntroduction((String) params.get("introduction"));
        this.updateById(doctor);

        //同步更新关联用户的姓名
        if(params.get("name") != null){
            User user = userService.getById(doctor.getUserId());
            if(user != null){
                user.setRealName((String) params.get("name"));
                userService.updateById(user);
            }
        }

        result.put("success", true);
        result.put("message", "更新医生信息成功");
        return result;
    }

    /**
     * 删除医生及其关联用户
     */
    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = this.getById(id);
        if(doctor != null){
            userService.removeById(doctor.getUserId());
            this.removeById(id);
        }
    }

}

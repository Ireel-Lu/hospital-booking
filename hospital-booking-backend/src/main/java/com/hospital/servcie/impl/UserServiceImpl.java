package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.dto.LoginDTO;
import com.hospital.dto.RegisterDTO;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.PatientProfile;
import com.hospital.entity.User;

import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.PatientProfileMapper;
import com.hospital.mapper.UserMapper;
import com.hospital.servcie.IUserService;
import com.hospital.servcie.OssFileService;
import com.hospital.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final JwtUtils jwtUtils;
    private final PatientProfileMapper patientProfileMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final OssFileService ossFileService;
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
     * 用户注册（仅患者）
     * @param registerDTO 注册参数：账号信息 + 患者明细信息
     * @return 注册结果
     */
    @Override
    @Transactional
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
        user.setGender(registerDTO.getGender());
        user.setRole("patient");//注册只产生患者，医生由管理员添加，管理员不开放注册
        user.setStatus(1);

        this.save(user);

        //保存患者明细，与user一对一
        PatientProfile profile = new PatientProfile();
        profile.setUserId(user.getId());
        profile.setGender(blankToNull(registerDTO.getGender()));
        profile.setBirthDate(registerDTO.getBirthDate());
        profile.setIdCard(blankToNull(registerDTO.getIdCard()));
        profile.setAddress(blankToNull(registerDTO.getAddress()));
        profile.setEmergencyContact(blankToNull(registerDTO.getEmergencyContact()));
        profile.setEmergencyContactPhone(blankToNull(registerDTO.getEmergencyContactPhone()));
        patientProfileMapper.insert(profile);
    }

    /**
     * 空字符串转null，避免唯一索引把空串当成重复身份证号
     */
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
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
        result.put("avatar",ossFileService.toSignedUrl(user.getAvatar()));

        return result;
    }

    /**
     * 查询用户列表
     */
    @Override
    public List<User> listUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(keyword != null && !keyword.trim().isEmpty()){
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        List<User> users = this.list(wrapper);
        //不把密码摘要返回给前端；私有 OSS 头像使用限时签名地址
        users.forEach(item -> {
            item.setPwd(null);
            item.setAvatar(ossFileService.toSignedUrl(item.getAvatar()));
        });
        return users;
    }

    /**
     * 更新用户状态
     */
    @Override
    public Map<String,Object> updateUserStatus(User user) {
        Map<String,Object> result = new HashMap<>();
        if(user.getId() == null || user.getStatus() == null){
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        User existingUser = this.getById(user.getId());
        if(existingUser == null){
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        existingUser.setStatus(user.getStatus());
        this.updateById(existingUser);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    /**
     * 编辑用户信息
     */
    @Override
    public Map<String,Object> updateUser(Map<String,Object> params) {
        Map<String,Object> result = new HashMap<>();
        Long id = Long.valueOf(params.get("id").toString());
        User user = this.getById(id);
        if(user == null){
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        //修改用户名，校检新用户名是否被其他用户占用
        if(params.get("username") != null){
            String newUsername = (String) params.get("username");
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, newUsername);
            if(this.count(wrapper) > 0){
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            user.setUsername(newUsername);
        }

        if(params.get("realName") != null) user.setRealName((String) params.get("realName"));
        if(params.get("phone") != null) user.setPhone((String) params.get("phone"));
        if(params.get("role") != null) user.setRole((String) params.get("role"));

        this.updateById(user);

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    /**
     * 重置用户密码
     */
    @Override
    public Map<String,Object> resetPassword(Map<String,Object> params) {
        Map<String,Object> result = new HashMap<>();
        Long id = Long.valueOf(params.get("id").toString());
        User user = this.getById(id);
        if(user == null){
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        //与登录校验保持一致，密码仍需MD5存储
        user.setPwd(DigestUtils.md5DigestAsHex(((String) params.get("pwd")).getBytes()));
        this.updateById(user);

        result.put("success", true);
        result.put("message", "密码重置成功");
        return result;
    }

    /**
     * 删除用户
     */
    @Override
    public void deleteUser(Long id) {
        this.removeById(id);
    }

    /**
     * 个人资料卡：账号信息 + 对应角色的明细信息
     */
    @Override
    public Map<String, Object> getProfile(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> result = baseUserInfo(user);

        if ("patient".equals(user.getRole())) {
            PatientProfile profile = getPatientProfileEntity(userId);
            if (profile != null) {
                result.put("profileId", profile.getId());
                result.put("birthDate", profile.getBirthDate());
                result.put("idCard", profile.getIdCard());
                result.put("address", profile.getAddress());
                result.put("emergencyContact", profile.getEmergencyContact());
                result.put("emergencyContactPhone", profile.getEmergencyContactPhone());
                if (result.get("gender") == null) {
                    result.put("gender", profile.getGender());
                }
            }
        } else if ("doctor".equals(user.getRole())) {
            Doctor doctor = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
            if (doctor != null) {
                result.put("doctorId", doctor.getId());
                result.put("employeeNo", doctor.getEmployeeNo());
                result.put("title", doctor.getTitle());
                result.put("specialty", doctor.getSpecialty());
                result.put("introduction", doctor.getIntroduction());
                result.put("consultationFee", doctor.getConsultationFee());
                result.put("departmentId", doctor.getDepartmentId());
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    if (department != null) {
                        result.put("departmentName", department.getName());
                    }
                }
                if (result.get("avatar") == null) {
                    result.put("avatar", ossFileService.toSignedUrl(doctor.getAvatar()));
                }
            }
        }

        return result;
    }

    /**
     * 修改个人资料（患者同步 patient_profile，医生同步 doctor）
     */
    @Override
    @Transactional
    public Map<String, Object> updateProfile(Long userId, Map<String, Object> params) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        //用户名：非空且与其他账号不重复
        if (params.get("username") != null) {
            String newUsername = params.get("username").toString().trim();
            if (newUsername.isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            if (!newUsername.equals(user.getUsername())) {
                long count = this.count(new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, newUsername)
                        .ne(User::getId, userId));
                if (count > 0) {
                    throw new RuntimeException("用户名已存在");
                }
                user.setUsername(newUsername);
            }
        }

        if (params.get("realName") != null) {
            user.setRealName(params.get("realName").toString().trim());
        }
        if (params.get("gender") != null) {
            user.setGender(normalizeGender(params.get("gender")));
        }
        if (params.get("phone") != null) {
            user.setPhone(params.get("phone").toString().trim());
        }
        if (params.get("email") != null) {
            user.setEmail(params.get("email").toString().trim());
        }
        this.updateById(user);

        if ("patient".equals(user.getRole())) {
            updatePatientProfile(userId, params);
        } else if ("doctor".equals(user.getRole())) {
            updateDoctorProfile(userId, user, params);
        }

        return getProfile(userId);
    }

    /**
     * 修改密码：校验原密码后按登录一致的方式重新 MD5 存储
     */
    @Override
    public void changePassword(Long userId, String oldPwd, String newPwd) {
        if (oldPwd == null || oldPwd.isBlank()) {
            throw new RuntimeException("请输入原密码");
        }
        if (newPwd == null || newPwd.trim().length() < 6) {
            throw new RuntimeException("新密码至少 6 位");
        }

        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String oldEncoded = DigestUtils.md5DigestAsHex(oldPwd.getBytes());
        if (!oldEncoded.equals(user.getPwd())) {
            throw new RuntimeException("原密码不正确");
        }

        user.setPwd(DigestUtils.md5DigestAsHex(newPwd.getBytes()));
        this.updateById(user);
    }

    /**
     * 更新头像：同步医生明细表中的头像
     */
    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        this.updateById(user);

        if ("doctor".equals(user.getRole())) {
            Doctor doctor = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
            if (doctor != null) {
                doctor.setAvatar(avatarUrl);
                doctorMapper.updateById(doctor);
            }
        }
    }

    /**
     * 患者资料卡（医生接诊时查看）
     */
    @Override
    public Map<String, Object> getPatientProfile(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("患者不存在");
        }

        Map<String, Object> result = baseUserInfo(user);
        PatientProfile profile = getPatientProfileEntity(userId);
        if (profile != null) {
            result.put("birthDate", profile.getBirthDate());
            result.put("idCard", profile.getIdCard());
            result.put("address", profile.getAddress());
            result.put("emergencyContact", profile.getEmergencyContact());
            result.put("emergencyContactPhone", profile.getEmergencyContactPhone());
            if (result.get("gender") == null) {
                result.put("gender", profile.getGender());
            }
        }
        return result;
    }

    /**
     * 组装账号基础信息
     */
    private Map<String, Object> baseUserInfo(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("gender", user.getGender());
        result.put("avatar", ossFileService.toSignedUrl(user.getAvatar()));
        result.put("role", user.getRole());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());
        return result;
    }

    /**
     * 查询患者明细（可能不存在，历史注册数据需容忍）
     */
    private PatientProfile getPatientProfileEntity(Long userId) {
        return patientProfileMapper.selectOne(
                new LambdaQueryWrapper<PatientProfile>().eq(PatientProfile::getUserId, userId));
    }

    /**
     * 更新患者明细
     */
    private void updatePatientProfile(Long userId, Map<String, Object> params) {
        PatientProfile profile = getPatientProfileEntity(userId);
        if (profile == null) {
            profile = new PatientProfile();
            profile.setUserId(userId);
        }

        if (params.get("gender") != null) {
            profile.setGender(normalizeGender(params.get("gender")));
        }
        if (params.containsKey("birthDate")) {
            String value = stringOrNull(params.get("birthDate"));
            profile.setBirthDate(value == null ? null : LocalDate.parse(value));
        }
        if (params.containsKey("idCard")) {
            String value = stringOrNull(params.get("idCard"));
            //身份证号有唯一索引，先检查是否被其他账号占用
            if (value != null && !value.equals(profile.getIdCard())) {
                long count = patientProfileMapper.selectCount(new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getIdCard, value)
                        .ne(PatientProfile::getUserId, userId));
                if (count > 0) {
                    throw new RuntimeException("该身份证号已被其他账号使用");
                }
            }
            profile.setIdCard(value);
        }
        if (params.containsKey("address")) {
            profile.setAddress(stringOrNull(params.get("address")));
        }
        if (params.containsKey("emergencyContact")) {
            profile.setEmergencyContact(stringOrNull(params.get("emergencyContact")));
        }
        if (params.containsKey("emergencyContactPhone")) {
            profile.setEmergencyContactPhone(stringOrNull(params.get("emergencyContactPhone")));
        }

        if (profile.getId() == null) {
            patientProfileMapper.insert(profile);
        } else {
            patientProfileMapper.updateById(profile);
        }
    }

    /**
     * 更新医生明细（姓名与账号保持一致）
     */
    private void updateDoctorProfile(Long userId, User user, Map<String, Object> params) {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            return;
        }

        if (params.get("realName") != null) {
            doctor.setName(user.getRealName());
        }
        if (params.containsKey("title")) {
            doctor.setTitle(stringOrNull(params.get("title")));
        }
        if (params.containsKey("specialty")) {
            doctor.setSpecialty(stringOrNull(params.get("specialty")));
        }
        if (params.containsKey("introduction")) {
            doctor.setIntroduction(stringOrNull(params.get("introduction")));
        }
        doctorMapper.updateById(doctor);
    }

    /**
     * 统一性别存储格式：user.gender 为 char(1)，只存"男"/"女"
     */
    private String normalizeGender(Object value) {
        if (value == null) {
            return null;
        }
        String gender = value.toString().trim();
        if (gender.isEmpty()) {
            return null;
        }
        if (gender.startsWith("女") || "female".equalsIgnoreCase(gender) || "f".equalsIgnoreCase(gender)) {
            return "女";
        }
        return "男";
    }

    /**
     * 空字符串转 null，便于把可空字段清空
     */
    private String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

}

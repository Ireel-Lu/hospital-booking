package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.dto.LoginDTO;
import com.hospital.dto.RegisterDTO;
import com.hospital.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserService extends IService<User> {

    /**
     * 用户登录
     * @param loginDTO 登录参数：username、pwd
     * @return Result 返回登录结果（用户信息和token）
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册参数：username、pwd、realName、phone
     * @return 注册结果
     */
    void register(RegisterDTO registerDTO);

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户的ID(通过过滤器注入）
     * @return 用户信息
     */
    Map<String,Object> getUserById(Long userId);

    /**
     * 查询用户列表
     * @param keyword 搜索关键字（用户名、真实姓名、手机号）
     * @return 用户列表
     */
    List<User> listUsers(String keyword);

    /**
     * 更新用户状态
     * @param user 包含id、status的用户对象
     * @return success：是否成功；message：结果信息
     */
    Map<String,Object> updateUserStatus(User user);

    /**
     * 编辑用户信息
     * @param params 用户信息参数：id、username、realName、phone、role
     * @return success：是否成功；message：结果信息
     */
    Map<String,Object> updateUser(Map<String,Object> params);

    /**
     * 重置用户密码
     * @param params 用户信息参数：id、pwd
     * @return success：是否成功；message：结果信息
     */
    Map<String,Object> resetPassword(Map<String,Object> params);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 获取个人资料卡（账号信息 + 患者/医生明细）
     * @param userId 当前登录用户ID
     * @return 资料信息
     */
    Map<String, Object> getProfile(Long userId);

    /**
     * 修改个人资料
     * @param userId 当前登录用户ID
     * @param params 允许修改：username、realName、gender、phone、email、
     *               birthDate、idCard、address、emergencyContact、emergencyContactPhone、
     *               title、specialty、introduction
     * @return 修改后的资料信息
     */
    Map<String, Object> updateProfile(Long userId, Map<String, Object> params);

    /**
     * 修改密码（校验原密码）
     * @param userId 当前登录用户ID
     * @param oldPwd 原密码（明文）
     * @param newPwd 新密码（明文）
     */
    void changePassword(Long userId, String oldPwd, String newPwd);

    /**
     * 更新头像地址
     * @param userId 用户ID
     * @param avatarUrl 头像访问地址
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 查询患者资料卡（医生接诊时查看患者信息）
     * @param userId 患者用户ID
     * @return 账号信息 + 患者明细
     */
    Map<String, Object> getPatientProfile(Long userId);
}

package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "user",autoResultMap = true)
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 用户ID（主键，自增）
    private String username;            // 用户名（登录账号）
    private String pwd;            // 密码（MD5加密）
    private String realName;            // 真实姓名
    private String phone;               // 手机号
    private String email;               // 邮箱
    private String role;                // 角色（patient:患者, doctor:医生, admin:管理员）
    private Integer status;             // 状态（0:禁用, 1:启用）
    private String avatar;              // 头像URL
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;   // 更新时间（自动填充）
}

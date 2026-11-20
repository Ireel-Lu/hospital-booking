package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者明细表
 * 与 user 表一对一（user_id 关联），role = patient 的用户在此保存就诊所需的个人信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "patient_profile", autoResultMap = true)
public class PatientProfile {
    @TableId(type = IdType.AUTO)
    private Long id;                        // 主键
    private Long userId;                    // 关联用户ID
    private String gender;                  // 性别
    private LocalDate birthDate;            // 出生日期
    private String idCard;                  // 身份证号
    private String address;                 // 联系地址
    private String emergencyContact;        // 紧急联系人
    private String emergencyContactPhone;   // 紧急联系人电话
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;       // 创建时间（自动填充）
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;       // 更新时间（自动填充）
}

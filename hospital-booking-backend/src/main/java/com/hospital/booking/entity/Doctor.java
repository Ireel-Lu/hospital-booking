package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.annotation.FieldFill.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "doctor",autoResultMap = true)
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 医生ID（主键，自增）
    private Long userId;                // 关联用户ID
    private String employeeNo;          // 工号
    private String name;                // 姓名
    private String title;               // 职称（如主任医师、副主任医师）
    private String specialty;           // 专长
    private String introduction;        // 简介
    private Long departmentId;          // 科室ID
    private BigDecimal consultationFee; // 挂号费
    private String avatar;              // 头像URL
    private Integer status;             // 状态（0:禁用, 1:启用）
    @TableField(fill = INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
    @TableField(fill = INSERT_UPDATE)
    private LocalDateTime updateTime;   // 更新时间（自动填充）
}

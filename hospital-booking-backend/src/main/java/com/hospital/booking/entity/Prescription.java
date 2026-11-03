package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "prescription",autoResultMap = true)
public class Prescription {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 处方ID（主键，自增）
    private Long medicalRecordId;       // 关联病历ID
    private String medicineName;        // 药品名称
    private String specification;       // 规格
    private String dosage;              // 剂量
    private Integer quantity;           // 数量
    private String remark;              // 备注
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
}

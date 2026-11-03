package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "medical_record",autoResultMap = true)
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 病历ID（主键，自增）
    private Long appointmentId;         // 关联预约ID
    private Long patientId;             // 患者ID
    private Long doctorId;              // 医生ID
    private String diagnosis;           // 诊断结果
    private String symptoms;            // 症状描述
    private String advice;              // 医嘱建议
    private LocalDateTime visitTime;    // 就诊时间
    private Integer status;             // 状态（0:无效, 1:有效）
    @TableField(fill = FieldFill.INSERT)
    // 创建时间（自动填充）
    private LocalDateTime createTime;
}

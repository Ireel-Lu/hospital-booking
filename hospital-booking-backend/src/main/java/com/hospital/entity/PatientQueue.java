package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "patient_queue",autoResultMap = true)
public class PatientQueue {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 队列ID（主键，自增）
    private Long appointmentId;         // 关联预约ID
    private Long doctorId;              // 医生ID
    private Integer queueNumber;        // 排队号
    private String status;              // 状态（waiting:等待中, calling:叫号中, visited:已就诊, skipped:已跳过）
    private LocalDateTime callTime;     // 叫号时间
    private LocalDateTime visitTime;    // 就诊时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
}

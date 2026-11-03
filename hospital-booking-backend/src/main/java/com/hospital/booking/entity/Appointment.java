package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "appointment",autoResultMap = true)
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 预约ID（主键，自增）
    private String appointmentNo;       // 预约编号（唯一标识）
    private Long patientId;             // 患者ID
    private Long doctorId;              // 医生ID
    private Long sourceSettingId;       // 号源设置ID
    private LocalDate appointmentDate;  // 预约日期
    private String timeSlot;            // 时段（如08:00-09:00）
    private String symptoms;            // 症状描述
    private String status;              // 状态（pending:待支付, paid:已支付, cancelled:已取消, completed:已完成）
    private Integer queueNumber;        // 排队号
    private BigDecimal amount;          // 预约金额（挂号费）
    private LocalDateTime payTime;      // 支付时间
    private LocalDateTime cancelTime;   // 取消时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
}

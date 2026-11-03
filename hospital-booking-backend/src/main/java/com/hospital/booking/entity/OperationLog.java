package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "operation_log",autoResultMap = true)
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 日志ID（主键，自增）
    private Long operatorId;            // 操作人ID
    private String operatorName;        // 操作人姓名
    private String operationType;       // 操作类型（如CREATE_APPOINTMENT、PAY_APPOINTMENT）
    private String targetType;          // 目标类型（如Appointment、User）
    private Long targetId;              // 目标ID
    private String content;             // 操作内容描述
    private String ipAddress;           // 操作人IP地址
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
}

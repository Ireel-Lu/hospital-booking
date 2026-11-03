package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "source_setting",autoResultMap = true)
public class SourceSetting {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 设置ID（主键，自增）
    private Long doctorId;              // 医生ID
    private LocalDate date;             // 日期
    private String timeSlot;            // 时段（如08:00-09:00）
    private Integer totalQuantity;      // 总号源数量
    private Integer remainingQuantity;  // 剩余号源数量
    private Integer status;             // 状态（0:禁用, 1:启用）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;   // 更新时间（自动填充）
}

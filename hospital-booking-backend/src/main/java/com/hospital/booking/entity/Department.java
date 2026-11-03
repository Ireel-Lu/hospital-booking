package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "department",autoResultMap = true)
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 科室ID（主键，自增）
    private String name;                // 科室名称（如内科、外科）
    private String code;                // 科室编码
    private String description;         // 科室描述
    private String location;            // 科室位置
    private String phone;               // 科室电话
    private Integer sortOrder;          // 排序号
    private Integer status;             // 状态（0:禁用, 1:启用）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;   // 创建时间（自动填充）
}

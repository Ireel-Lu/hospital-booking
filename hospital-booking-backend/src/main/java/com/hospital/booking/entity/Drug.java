package com.hospital.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "drug",autoResultMap = true)
public class Drug {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 药品ID（主键，自增）
    private String name;                // 药品名称
    private String specification;       // 规格
    private String manufacturer;        // 生产厂家
    private BigDecimal price;           // 价格
    private Integer stock;              // 库存数量
    private String unit;                // 单位（如盒、瓶）
    private String category;            // 类别（如西药、中药）
    private String description;         // 描述
    private Integer status;             // 状态（0:禁用, 1:启用）
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}

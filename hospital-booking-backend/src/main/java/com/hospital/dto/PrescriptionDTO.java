package com.hospital.dto;

import lombok.Data;

@Data
public class PrescriptionDTO {
    private Long medicalRecordId;  // 关联病历ID
    private String medicineName;   // 药品名称
    private String specification;  // 规格
    private String dosage;  // 剂量
    private Integer quantity; // 数量
    private String remark; // 备注
}

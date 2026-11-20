package com.hospital.dto;

import lombok.Data;

@Data
public class MedicalRecordDTO {

    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String diagnosis; // 诊断结果
    private String symptoms; // 症状描述
    private String advice; // 医嘱建议
}

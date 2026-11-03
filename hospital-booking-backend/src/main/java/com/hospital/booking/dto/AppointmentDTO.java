package com.hospital.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentDTO {
    private Long patientId;

    @NotBlank(message = "医生ID不可为空")
    private Long doctorId;

    @NotBlank(message = "号源设置ID不能为空")
    private Long sourceSettingId;

    private String symptoms;
}

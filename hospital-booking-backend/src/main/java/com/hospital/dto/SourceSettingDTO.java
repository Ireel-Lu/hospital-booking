package com.hospital.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SourceSettingDTO {

    private Long doctorId;
    private LocalDate date;
    private String timeSlot;
    private Integer totalQuantity;

}

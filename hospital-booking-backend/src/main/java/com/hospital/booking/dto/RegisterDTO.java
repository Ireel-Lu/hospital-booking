package com.hospital.booking.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String pwd;
    private String realName;
    private String phone;
}

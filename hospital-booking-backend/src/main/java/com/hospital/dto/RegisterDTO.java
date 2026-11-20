package com.hospital.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterDTO {
    private String username;
    private String pwd;
    private String realName;
    private String phone;
    /* 以下为患者明细信息，注册时可一并填写 */
    private String gender;                  // 性别
    private LocalDate birthDate;            // 出生日期
    private String idCard;                  // 身份证号
    private String address;                 // 联系地址
    private String emergencyContact;        // 紧急联系人
    private String emergencyContactPhone;   // 紧急联系人电话
}

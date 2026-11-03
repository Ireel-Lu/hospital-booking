package com.hospital.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hospital.booking.mapper")
public class HospitalAppointmentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalAppointmentSystemApplication.class, args);
    }
}

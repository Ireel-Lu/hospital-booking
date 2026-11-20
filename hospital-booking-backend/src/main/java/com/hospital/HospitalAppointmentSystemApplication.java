package com.hospital;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hospital.mapper")
public class HospitalAppointmentSystemApplication {

    public static void main(String[] args) {

        SpringApplication.run(HospitalAppointmentSystemApplication.class, args);

        System.out.println("(●'◡'●)运行成功了哦(ง •_•)ง加油呀~ \n o(*￣▽￣*)ブ");
    }
}

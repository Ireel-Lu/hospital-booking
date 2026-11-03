package com.hospital.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.booking.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}

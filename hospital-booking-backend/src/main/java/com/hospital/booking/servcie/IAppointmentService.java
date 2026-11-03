package com.hospital.booking.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.booking.dto.AppointmentDTO;
import com.hospital.booking.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface IAppointmentService extends IService<Appointment> {

    Map<String, Object> createAppointment(AppointmentDTO appointmentDTO);

    boolean payAppointment(Long id);

    boolean cancelAppointment(Long id, String reason);

    List<Map<String,Object>> getUserAppointments(Long userId);

    List<Map<String,Object>> filterAppointments(Map<String, Object> params);
}

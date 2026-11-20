package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.dto.AppointmentDTO;
import com.hospital.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface IAppointmentService extends IService<Appointment> {

    Map<String, Object> createAppointment(AppointmentDTO appointmentDTO);

    boolean payAppointment(Long id, Long operatorId, boolean admin);

    boolean cancelAppointment(Long id, String reason, Long operatorId, boolean admin);

    List<Map<String,Object>> getUserAppointments(Long userId);

    List<Map<String,Object>> filterAppointments(Map<String, Object> params);

    /**
     * 获取预约详情（含患者、医生信息）
     * @param id 预约ID
     * @return 预约详情，预约不存在时返回null
     */
    Map<String,Object> getAppointmentDetail(Long id);

    /**
     * 完成就诊
     * @param id 预约ID
     * @return 是否成功，预约不存在时返回false
     */
    boolean completeAppointment(Long id);
}

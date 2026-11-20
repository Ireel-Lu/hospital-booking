package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.PatientQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface PatientQueueMapper extends BaseMapper<PatientQueue> {
    /**
     * 获取医生当前等待队列
     */
    @Select("SELECT " +
            "q.id, q.queue_number, q.status, q.create_time, " +
            "q.appointment_id, " +
            "a.appointment_no, a.time_slot, a.symptoms, " +
            "u.real_name as patient_name, u.phone as patient_phone " +
            "FROM hospital_appointment.patient_queue q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN user u ON a.patient_id = u.id " +
            "WHERE q.doctor_id = #{doctorId} " +
            "AND q.status IN ('waiting', 'calling') " +
            "AND a.appointment_date = CURDATE() " +
            "ORDER BY q.queue_number ASC")
    List<Map<String, Object>> getWaitingQueue(Long doctorId);

    /**
     * 获取当前正在叫号中的患者
     */
    @Select("SELECT " +
            "q.id, q.queue_number, q.appointment_id, " +
            "u.real_name as patient_name " +
            "FROM hospital_appointment.patient_queue q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN user u ON a.patient_id = u.id " +
            "WHERE q.doctor_id = #{doctorId} " +
            "AND q.status = 'calling' " +
            "AND a.appointment_date = CURDATE() " +
            "LIMIT 1")
    Map<String, Object> getCallingPatientByDoctorId(Long doctorId);

    @Select("SELECT " +
            "q.id, q.queue_number, q.appointment_id, " +
            "u.real_name as patient_name, u.phone as patient_phone " +
            "FROM hospital_appointment.patient_queue q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN user u ON a.patient_id = u.id " +
            "WHERE q.doctor_id = #{doctorId} " +
            "AND q.status = 'waiting' " +
            "AND a.appointment_date = CURDATE() " +
            "ORDER BY q.queue_number ASC " +
            "LIMIT 1")
    Map<String, Object> getNextPatientByDoctorId(Long doctorId);

    /**
     * 更新队列状态
     */
    @Update("UPDATE hospital_appointment.patient_queue SET status = #{status}, call_time = NOW() WHERE id = #{queueId}")
    void updateStatus(@Param("queueId") Long queueId, @Param("status") String status);

    /**
     * 获取指定队列号之后的下一位等待患者
     */
    @Select("SELECT " +
            "q.id, q.queue_number, q.appointment_id, " +
            "u.real_name as patient_name " +
            "FROM hospital_appointment.patient_queue q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN user u ON a.patient_id = u.id " +
            "WHERE q.doctor_id = #{doctorId} " +
            "AND q.status = 'waiting' " +
            "AND q.queue_number > #{queueNumber} " +
            "AND a.appointment_date = CURDATE() " +
            "ORDER BY q.queue_number ASC " +
            "LIMIT 1")
    Map<String, Object> getNextPatient(@Param("doctorId") Long doctorId, @Param("queueNumber") Integer queueNumber);

    /**
     * 统计医生当前等待中患者人数
     */
    @Select("SELECT COUNT(*) FROM hospital_appointment.patient_queue q " +
            "LEFT JOIN appointment a ON q.appointment_id = a.id " +
            "WHERE q.doctor_id = #{doctorId} " +
            "AND q.status = 'waiting' " +
            "AND a.appointment_date = CURDATE()")
    Integer countWaitingPatients(Long doctorId);

    /**
     * 患者侧：查询自己今天的候诊进度（当前叫号、前面还有几人）
     */
    @Select("SELECT " +
            "q.id AS queue_id, q.queue_number, q.status AS queue_status, " +
            "a.id AS appointment_id, a.appointment_no, a.appointment_date, a.time_slot, " +
            "a.status AS appointment_status, " +
            "d.id AS doctor_id, d.name AS doctor_name, d.title AS doctor_title, " +
            "dept.name AS department_name, " +
            "(SELECT q2.queue_number FROM patient_queue q2 " +
            "   JOIN appointment a2 ON q2.appointment_id = a2.id " +
            "  WHERE q2.doctor_id = q.doctor_id AND q2.status = 'calling' " +
            "    AND a2.appointment_date = CURDATE() ORDER BY q2.queue_number LIMIT 1) AS calling_number, " +
            "(SELECT COUNT(*) FROM patient_queue q3 " +
            "   JOIN appointment a3 ON q3.appointment_id = a3.id " +
            "  WHERE q3.doctor_id = q.doctor_id AND q3.status = 'waiting' " +
            "    AND a3.appointment_date = CURDATE() AND q3.queue_number < q.queue_number) AS ahead_count " +
            "FROM patient_queue q " +
            "JOIN appointment a ON q.appointment_id = a.id " +
            "LEFT JOIN doctor d ON q.doctor_id = d.id " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "WHERE a.patient_id = #{userId} " +
            "AND a.appointment_date = CURDATE() " +
            "AND q.status IN ('waiting', 'calling') " +
            "ORDER BY q.queue_number ASC LIMIT 1")
    Map<String, Object> getPatientQueueStatus(Long userId);

}

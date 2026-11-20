package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    @Select("SELECT " +
            "mr.*, " +
            "d.name as doctor_name, " +
            "d.title as doctor_title, " +
            "dept.name as department_name, " +
            "a.appointment_date, " +
            "a.time_slot, " +
            "a.appointment_no " +
            "FROM medical_record mr " +
            "LEFT JOIN appointment a ON mr.appointment_id = a.id " +
            "LEFT JOIN doctor d ON mr.doctor_id = d.id " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "WHERE mr.patient_id = #{patientId} " +
            "ORDER BY mr.visit_time DESC")
    List<Map<String, Object>> getPatientRecords(@Param("patientId") Long patientId);

    @Update("UPDATE medical_record " +
            "SET image_urls = JSON_ARRAY_APPEND(COALESCE(NULLIF(image_urls, ''), JSON_ARRAY()), '$', #{url}) " +
            "WHERE id = #{recordId}")
    int appendImage(@Param("recordId") Long recordId, @Param("url") String url);

    List<Map<String, Object>> getDoctorRecords(@Param("doctorId") Long doctorId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}

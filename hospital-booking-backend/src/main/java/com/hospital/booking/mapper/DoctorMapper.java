package com.hospital.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.booking.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    @Select("SELECT " +
            "d.id, d.name, d.title, d.specialty, d.consultation_fee, " +
            "dept.name as department_name " +
            "FROM doctor d " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "WHERE d.status = 1 " +
            "AND (d.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.specialty LIKE CONCAT('%', #{keyword}, '%') " +
            "OR dept.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY d.id ASC")
    List<Map<String, Object>> searchDoctors(String keyword);

    @Select("select d.id,d.name,d.specialty," +
            "count(a.id) as apppointment_count" +
            " from doctor d " +
            "left join appointment a on d.id = a.doctor_id " +
            "and a.appointment_date >= DATE_SUB(CURDATE(),INTERVAL 30 DAY )" +
            "and a.status = 'paid'" +
            "where d.status = 1 " +
            "group by d.id " +
            "order by apppointment_count desc " +
            "limit #{limit}")
    List<Map<String, Object>> getPopularDoctors(int limit);
}

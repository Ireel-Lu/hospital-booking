package com.hospital.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.booking.entity.SourceSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface SourceSettingMapper extends BaseMapper<SourceSetting> {

    /**
     * 获取医生某天所有时段号源
     */
    @Select("SELECT " +
            "ss.*, " +
            "CASE WHEN ss.remaining_quantity > 0 THEN 'available' ELSE 'full' END as slot_status " +
            "FROM source_setting ss " +
            "WHERE ss.doctor_id = #{doctorId} " +
            "AND ss.date = #{date} " +
            "AND ss.status = 1 " +
            "ORDER BY FIELD(ss.time_slot, '08:00-09:00', '09:00-10:00', '10:00-11:00', " +
            "'11:00-12:00', '14:00-15:00', '15:00-16:00', '16:00-17:00')")
    List<Map<String, Object>> getDoctorSettings(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);


}

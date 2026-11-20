package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
    /**
     * 获取就诊记录下的所有处方
     */
    @Select("SELECT * FROM prescription WHERE medical_record_id = #{medicalRecordId} ORDER BY create_time ASC")
    List<Map<String, Object>> getByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

}

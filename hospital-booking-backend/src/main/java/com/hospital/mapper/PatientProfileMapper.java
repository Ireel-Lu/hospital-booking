package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.PatientProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者明细Mapper
 */
@Mapper
public interface PatientProfileMapper extends BaseMapper<PatientProfile> {
}

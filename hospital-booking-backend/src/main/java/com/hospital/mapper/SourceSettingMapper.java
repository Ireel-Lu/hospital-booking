package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.SourceSetting;
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
    List<Map<String, Object>> getDoctorSettings(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);


}

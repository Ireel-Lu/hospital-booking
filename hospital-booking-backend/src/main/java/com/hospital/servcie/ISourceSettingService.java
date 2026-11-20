package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.dto.SourceSettingDTO;
import com.hospital.entity.SourceSetting;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface ISourceSettingService extends IService<SourceSetting> {
    /**
     * 保存号源设置
     * @param dto 号源设置参数
     */
    void saveSetting(SourceSettingDTO dto);

    /**
     * 获取医生某天的号源设置列表
     * @param doctorId 医生ID
     * @param date 日期
     * @return 号源设置列表
     */
    List<Map<String,Object>> getSettings(Long doctorId, LocalDate date);
}

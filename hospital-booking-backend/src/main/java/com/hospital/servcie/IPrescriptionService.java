package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.dto.PrescriptionDTO;
import com.hospital.entity.Prescription;

import java.util.List;
import java.util.Map;

public interface IPrescriptionService<P> extends IService<Prescription> {
    /**
     * 添加处方
     * @param dto 处方请求参数
     */
    void addPrescription(PrescriptionDTO dto);

    /**
     * 批量添加处方
     * @param dtos 处方请求参数列表
     */
    void addPrescriptions(List<PrescriptionDTO> dtos);

    /**
     * 获取就诊记录下的处方列表
     * @param medicalRecordId 病历ID
     * @return 处方列表
     */
    List<Map<String, Object>> getByRecordId(Long medicalRecordId);
}

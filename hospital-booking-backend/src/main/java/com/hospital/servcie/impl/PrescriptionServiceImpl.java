package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.dto.PrescriptionDTO;
import com.hospital.entity.Prescription;
import com.hospital.mapper.PrescriptionMapper;
import com.hospital.servcie.IPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper,Prescription> implements IPrescriptionService<Prescription> {

    private final PrescriptionMapper prescriptionMapper;

    @Override
    public void addPrescription(PrescriptionDTO dto) {
        Prescription prescription = new Prescription();
        prescription.setMedicalRecordId(dto.getMedicalRecordId());  // 关联病历ID
        prescription.setMedicineName(dto.getMedicineName());        // 药品名称
        prescription.setSpecification(dto.getSpecification());      // 规格
        prescription.setDosage(dto.getDosage());                    // 用法用量
        prescription.setQuantity(dto.getQuantity());                // 数量
        prescription.setRemark(dto.getRemark());                    // 备注
        this.save(prescription);  // 保存到数据库
    }

    @Override
    public void addPrescriptions(List<PrescriptionDTO> dtos) {
        List<Prescription> prescriptions = new ArrayList<>();
        for (PrescriptionDTO dto : dtos) {
            Prescription prescription = new Prescription();
            prescription.setMedicalRecordId(dto.getMedicalRecordId());
            prescription.setMedicineName(dto.getMedicineName());
            prescription.setSpecification(dto.getSpecification());
            prescription.setDosage(dto.getDosage());
            prescription.setQuantity(dto.getQuantity());
            prescription.setRemark(dto.getRemark());
            prescriptions.add(prescription);
        }
        // 批量插入（使用MyBatis-Plus的saveBatch方法）
        this.saveBatch(prescriptions);
    }

    @Override
    public List<Map<String, Object>> getByRecordId(Long medicalRecordId) {
        return prescriptionMapper.getByMedicalRecordId(medicalRecordId);
    }
}

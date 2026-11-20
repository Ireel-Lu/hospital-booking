package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.dto.MedicalRecordDTO;
import com.hospital.entity.MedicalRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IMedicalRecordService extends IService<MedicalRecord> {
    MedicalRecord createRecord(MedicalRecordDTO dto);

    List<Map<String, Object>>  getByPatient(Long patientId);

    Map<String, Object> uploadImage(Long recordId, Long operatorId, boolean admin, MultipartFile file);

    public List<Map<String, Object>> getByDoctor(Long doctorId, String startDate, String endDate);
}

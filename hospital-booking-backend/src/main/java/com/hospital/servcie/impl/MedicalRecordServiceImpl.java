package com.hospital.servcie.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.dto.MedicalRecordDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.PatientQueue;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.MedicalRecordMapper;
import com.hospital.mapper.PatientQueueMapper;
import com.hospital.servcie.IMedicalRecordService;
import com.hospital.servcie.OssFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements IMedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;  // 病历数据访问
    private final AppointmentMapper appointmentMapper;      // 预约数据访问
    private final PatientQueueMapper patientQueueMapper;
    private final DoctorMapper doctorMapper;
    private final OssFileService ossFileService;

    @Override
    public MedicalRecord createRecord(MedicalRecordDTO dto) {
        log.info("创建就诊记录: appointmentId={}", dto.getAppointmentId());

        // ----- 1. 从预约中获取患者ID和医生ID -----
        Appointment appointment = appointmentMapper.selectById(dto.getAppointmentId());
        if (appointment == null) {
            throw new RuntimeException("预约不存在");  // 预约不存在时抛出异常
        }

        // ----- 2. 创建就诊记录（病历） -----
        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(dto.getAppointmentId());      // 关联预约ID
        record.setPatientId(appointment.getPatientId());      // 患者ID（从预约获取）
        // 🔥 修复：使用预约中的医生ID，而不是前端传递的用户ID
        // 前端传递的doctorId是用户表ID，但病历表需要医生表ID
        // 两者可能不同，必须从预约中获取准确的医生ID
        record.setDoctorId(appointment.getDoctorId());
        record.setDiagnosis(dto.getDiagnosis());
        record.setSymptoms(dto.getSymptoms());
        record.setAdvice(dto.getAdvice());
        record.setVisitTime(LocalDateTime.now());
        record.setStatus(1); // 状态：启用

        this.save(record);  // 保存病历到数据库
        log.info("就诊记录保存成功, id={}", record.getId());

        // ----- 3. 更新预约状态为已完成 -----
        appointment.setStatus("completed");
        appointmentMapper.updateById(appointment);
        log.info("预约状态更新为completed, appointmentId={}", appointment.getId());

        // ----- 4. 更新队列状态为已就诊 -----
        // 修改：使用 selectList 代替 selectOne，避免重复记录报错
        // 使用selectOne时如果有多条记录会抛出异常
        // 使用selectList更安全，可以处理多条记录的情况
        LambdaQueryWrapper<PatientQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientQueue::getAppointmentId, dto.getAppointmentId());
        List<PatientQueue> queues = patientQueueMapper.selectList(wrapper);

        if (queues != null && !queues.isEmpty()) {
            // 遍历所有队列记录，更新状态
            for (PatientQueue queue : queues) {
                queue.setStatus("visited");
                queue.setVisitTime(LocalDateTime.now());
                patientQueueMapper.updateById(queue);
                log.info("队列状态更新为visited, queueId={}", queue.getId());
            }
        }

        return record;
    }

    @Override
    public List<Map<String, Object>> getByPatient(Long patientId) {
        return withImages(medicalRecordMapper.getPatientRecords(patientId));
    }

    @Override
    public Map<String, Object> uploadImage(Long recordId, Long operatorId, boolean admin, MultipartFile file) {
        MedicalRecord record = this.getById(recordId);
        if (record == null || record.getStatus() == null || record.getStatus() != 1) {
            throw new RuntimeException("病历不存在或已失效");
        }
        if (!admin) {
            Long doctorId = doctorMapper.getDoctorIdByUserId(operatorId);
            if (doctorId == null || !doctorId.equals(record.getDoctorId())) {
                throw new AccessDeniedException("只能为自己的病历上传图片");
            }
        }

        List<String> images = parseImages(record.getImageUrls());
        if (images.size() >= 9) {
            throw new RuntimeException("每份病历最多上传 9 张图片");
        }

        String url = ossFileService.uploadCaseImage(file, recordId);
        try {
            if (medicalRecordMapper.appendImage(recordId, url) != 1) {
                throw new RuntimeException("病历图片地址保存失败");
            }
        } catch (RuntimeException e) {
            ossFileService.deleteByUrl(url);
            throw e;
        }

        images.add(url);
        Map<String, Object> result = new HashMap<>();
        result.put("url", ossFileService.toSignedUrl(url));
        result.put("images", images.stream().map(ossFileService::toSignedUrl).toList());
        return result;
    }

    @Override
    public List<Map<String, Object>> getByDoctor(Long doctorId, String startDate, String endDate) {
        return withImages(medicalRecordMapper.getDoctorRecords(doctorId, null, null));
    }

    private List<Map<String, Object>> withImages(List<Map<String, Object>> records) {
        for (Map<String, Object> record : records) {
            Object imageUrls = record.containsKey("image_urls") ? record.get("image_urls") : record.get("imageUrls");
            record.put("images", parseImages(imageUrls).stream()
                    .map(ossFileService::toSignedUrl).toList());
        }
        return records;
    }

    private List<String> parseImages(Object imageUrls) {
        if (imageUrls == null || imageUrls.toString().isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(JSON.parseArray(imageUrls.toString(), String.class));
        } catch (Exception e) {
            log.warn("病历图片地址解析失败：{}", e.getMessage());
            return new ArrayList<>();
        }
    }
}

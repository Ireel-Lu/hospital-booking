package com.hospital.controller;

import com.hospital.dto.PrescriptionDTO;
import com.hospital.dto.MedicalRecordDTO;
import com.hospital.entity.Result;
import com.hospital.servcie.IMedicalRecordService;
import com.hospital.servcie.IPrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "病历管理", description = "MedicalRecordController")
@RestController
@RequestMapping("/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController {
    private final IMedicalRecordService medicalRecordService;

    private final IPrescriptionService prescriptionService;

    /**
     * 创建病历记录
     *
     * @param dto 病历请求参数
     *   - patientId: 患者ID
     *   - doctorId: 医生ID
     *   - appointmentId: 关联的预约ID
     *   - diagnosis: 诊断结果
     *   - advice: 医嘱
     * @return 统一响应对象，包含创建的病历记录
     */
    @Operation(summary = "创建病历（create）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/create")
    public Result create(@RequestBody MedicalRecordDTO dto) {
        // 调用服务层创建病历
        // 返回创建的病历对象
        return Result.success(medicalRecordService.createRecord(dto));
    }

    /**
     * 查询患者的病历列表
     * @param patientId 患者ID
     * @return 统一响应对象，包含病历列表
     */
    @Operation(summary = "按患者查病历（getByPatient）")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}")
    public Result getByPatient(@PathVariable Long patientId,
                               @RequestAttribute Long userId,
                               @RequestAttribute String role) {
        if ("patient".equals(role) && !userId.equals(patientId)) {
            throw new AccessDeniedException("无权查看他人的病历");
        }
        return Result.success(medicalRecordService.getByPatient(patientId));
    }

    @Operation(summary = "上传病历图片")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{recordId}/images")
    public Result uploadImage(@PathVariable Long recordId,
                              @RequestAttribute Long userId,
                              @RequestAttribute String role,
                              @RequestParam("file") MultipartFile file) {
        return Result.success(medicalRecordService.uploadImage(
                recordId, userId, "admin".equals(role), file));
    }

    /**
     * 添加处方
     * @param dto 处方请求参数
     *   - medicalRecordId: 关联的病历ID
     *   - medicineName: 药品名称
     *   - dosage: 用法用量
     *   - quantity: 数量
     * @return 统一响应对象
     */
    @Operation(summary = "添加处方（addPrescription）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/prescription/add")
    public Result addPrescription(@RequestBody PrescriptionDTO dto) {
        // 调用服务层添加处方
        prescriptionService.addPrescription(dto);
        return Result.success("添加成功");
    }

    /**
     * 查询病历关联的处方列表
     * @param recordId 病历ID
     * @return 统一响应对象，包含处方列表
     */
    @Operation(summary = "查询病历处方列表（getPrescriptions）")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/prescription/{recordId}")
    public Result getPrescriptions(@PathVariable Long recordId) {
        // 调用服务层查询该病历的所有处方
        return Result.success(prescriptionService.getByRecordId(recordId));
    }

}

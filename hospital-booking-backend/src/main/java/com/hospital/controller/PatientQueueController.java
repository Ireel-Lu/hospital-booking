package com.hospital.controller;


import com.hospital.entity.Result;
import com.hospital.servcie.IPatientQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "候诊叫号", description = "PatientQueueController")
@PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
@RestController
@RequestMapping("/patient-queue")
@RequiredArgsConstructor
public class PatientQueueController {

    private final IPatientQueueService patientQueueService;

    /**
     * 获取医生当前的队列状态
     */
    @Operation(summary = "查询队列状态（getQueueStatusByUserId）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/status/{userId}")
    public Result getQueueStatusByUserId(@PathVariable Long userId) {
        return Result.success(patientQueueService.getQueueStatusByUserId(userId));
    }

    /**
     * 呼叫下一位患者
     */
    @Operation(summary = "呼叫下一位（callNextPatient）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/call-next/{userId}")
    public Result callNextPatient(@PathVariable Long userId) {
        return Result.success(patientQueueService.callNextPatientByUserId(userId));
    }

    /**
     * 患者查看自己的候诊进度（当前叫号、我的号、前面还有几人）
     */
    @Operation(summary = "查看我的候诊进度（getMyQueueStatus）")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @GetMapping("/my")
    public Result getMyQueueStatus(@RequestAttribute Long userId) {
        return Result.success(patientQueueService.getPatientQueueStatusByUserId(userId));
    }

    /**
     * 跳过当前患者
     */
    @Operation(summary = "跳过当前患者（skip）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/skip/{queueId}")
    public Result skip(@PathVariable Long queueId) {
        patientQueueService.skipCurrentPatient(queueId);
        return Result.success("已跳过当前患者");
    }
}

package com.hospital.controller;

import com.hospital.servcie.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据导出", description = "ExportController")
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * 导出患者病历记录
     *
     * 接口路径：GET /export/medical-records/patient/{patientId}
     * 路径参数：患者ID
     *
     * 导出内容：
     * - 预约单号、就诊日期、时段
     * - 医生姓名、职称、科室
     * - 症状描述、诊断结果、医嘱
     * - 就诊时间
     *
     * 使用场景：
     * - 患者下载自己的就诊记录
     * - 方便患者整理病历资料
     *
     * @param patientId 患者ID
     * @param response  HTTP响应对象，用于写入Excel文件
     */
    @Operation(summary = "导出患者就诊记录（exportPatientRecords）")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @GetMapping("/medical-records/patient/{patientId}")
    public void exportPatientRecords(@PathVariable("patientId") Long patientId, HttpServletResponse response) {
        exportService.exportPatientMedicalRecords(patientId, response);
    }

    /**
     * 导出医生接诊记录
     *
     * 接口路径：GET /export/medical-records/doctor/{doctorId}
     * 路径参数：医生ID
     *
     * 导出内容：
     * - 患者姓名、电话
     * - 就诊日期、时段
     * - 症状描述、诊断结果、医嘱
     * - 就诊时间
     *
     * 使用场景：
     * - 医生统计自己的接诊情况
     * - 医院管理考核医生工作量
     *
     * @param doctorId 医生ID
     * @param response HTTP响应对象，用于写入Excel文件
     */
    @Operation(summary = "导出医生接诊记录（exportDoctorRecord）")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/medical-records/doctor/{doctorId}")
    public void exportDoctorRecord(@PathVariable("doctorId") Long doctorId, HttpServletResponse response) {
        exportService.exportDoctorMedicalRecords(doctorId, response);
    }

    /**
     * 导出预约记录
     *
     * 接口路径：GET /export/appointments
     * 请求参数（Query String，均为可选）：
     * - departmentId: 科室ID，筛选指定科室的预约
     * - startDate: 开始日期（格式：yyyy-MM-dd）
     * - endDate: 结束日期（格式：yyyy-MM-dd）
     *
     * 导出内容：
     * - 预约单号、就诊日期、时段
     * - 患者姓名、医生姓名
     * - 状态、金额
     * - 创建时间
     *
     * 使用场景：
     * - 管理员导出预约报表
     * - 数据分析和统计
     *
     * @param departmentId 科室ID（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param response     HTTP响应对象，用于写入Excel文件
     */
    @Operation(summary = "导出预约记录（exportAppointments）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/appointments")
    public void exportAppointments(@RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        // 调用服务层执行导出，传递筛选条件
        exportService.exportAppointments(departmentId, startDate, endDate, response);
    }

    /**
     * 导出科室统计报表
     *
     * 接口路径：GET /export/department-statistics
     * 统计每个科室在指定时间范围内的预约数量
     *
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate   结束日期（可选，格式：yyyy-MM-dd）
     * @param response  HTTP响应对象，用于写入Excel文件
     */
    @Operation(summary = "导出科室统计报表（exportDepartmentStatistics）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/department-statistics")
    public void exportDepartmentStatistics(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        exportService.exportDepartmentStatistics(startDate, endDate, response);
    }
}

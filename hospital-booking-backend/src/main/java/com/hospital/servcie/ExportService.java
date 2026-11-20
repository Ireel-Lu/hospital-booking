package com.hospital.servcie;

import jakarta.servlet.http.HttpServletResponse;

public interface ExportService {

    /**
     * 导出患者就诊记录
     * @param patientId 患者ID
     * @param response HTTP响应对象，用于写入Excel文件
     */
    void exportPatientMedicalRecords(Long patientId, HttpServletResponse response);

    /**
     * 导出医生接诊记录
     * @param doctorId 医生ID
     * @param response HTTP响应对象，用于写入Excel文件
     */
    void exportDoctorMedicalRecords(Long doctorId, HttpServletResponse response);

    /**
     * 导出预约记录
     * @param departmentId 科室ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param response HTTP响应对象，用于写入Excel文件
     */
    void exportAppointments(Long departmentId, String startDate, String endDate, HttpServletResponse response);

    /**
     * 导出科室统计报表
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param response HTTP响应对象，用于写入Excel文件
     */
    void exportDepartmentStatistics(String startDate, String endDate, HttpServletResponse response);

}

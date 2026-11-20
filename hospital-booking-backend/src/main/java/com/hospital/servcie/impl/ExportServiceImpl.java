package com.hospital.servcie.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.mapper.*;
import com.hospital.servcie.ExportService;
import com.hospital.vo.AppointmentExportVO;
import com.hospital.vo.DepartmentStatExportVO;
import com.hospital.vo.DoctorRecordExportVO;
import com.hospital.vo.PatientRecordExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final AppointmentMapper appointmentMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    /**
     * 导出患者就诊记录
     * 文件名格式：{患者姓名}_就诊记录.xlsx
     */
    @Override
    public void exportPatientMedicalRecords(Long patientId, HttpServletResponse response) {
        try {
            User patient = userMapper.selectById(patientId);
            if (patient == null) {
                throw new RuntimeException("患者不存在");
            }

            List<Map<String, Object>> records = medicalRecordMapper.getPatientRecords(patientId);
            List<PatientRecordExportVO> exportData = new ArrayList<>();
            for (Map<String, Object> record : records) {
                PatientRecordExportVO vo = new PatientRecordExportVO();
                vo.setAppointmentNo(toText(record.get("appointment_no")));
                vo.setDoctorName(toText(record.get("doctor_name")));
                vo.setDoctorTitle(toText(record.get("doctor_title")));
                vo.setDepartmentName(toText(record.get("department_name")));
                vo.setAppointmentDate(toText(record.get("appointment_date")));
                vo.setTimeSlot(toText(record.get("time_slot")));
                vo.setSymptoms(toText(record.get("symptoms")));
                vo.setDiagnosis(toText(record.get("diagnosis")));
                vo.setAdvice(toText(record.get("advice")));
                vo.setVisitTime(toText(record.get("visit_time")));
                exportData.add(vo);
            }

            String name = patient.getRealName() == null ? "患者" : patient.getRealName();
            writeExcel(response, name + "_就诊记录", "就诊记录", PatientRecordExportVO.class, exportData);
            log.info("导出患者就诊记录成功：patientId={}, 记录数={}", patientId, exportData.size());
        } catch (IOException e) {
            log.error("导出就诊记录失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出医生接诊记录
     * 文件名格式：{医生姓名}_接诊记录.xlsx
     */
    @Override
    public void exportDoctorMedicalRecords(Long doctorId, HttpServletResponse response) {
        try {
            Doctor doctor = doctorMapper.selectById(doctorId);
            if (doctor == null) {
                throw new RuntimeException("医生不存在");
            }

            List<Map<String, Object>> records = medicalRecordMapper.getDoctorRecords(doctorId, null, null);
            List<DoctorRecordExportVO> exportData = new ArrayList<>();
            for (Map<String, Object> record : records) {
                DoctorRecordExportVO vo = new DoctorRecordExportVO();
                vo.setPatientName(toText(record.get("patient_name")));
                vo.setPatientPhone(toText(record.get("patient_phone")));
                vo.setAppointmentDate(toText(record.get("appointment_date")));
                vo.setTimeSlot(toText(record.get("time_slot")));
                vo.setSymptoms(toText(record.get("symptoms")));
                vo.setDiagnosis(toText(record.get("diagnosis")));
                vo.setAdvice(toText(record.get("advice")));
                vo.setVisitTime(toText(record.get("visit_time")));
                exportData.add(vo);
            }

            String name = doctor.getName() == null ? "医生" : doctor.getName();
            writeExcel(response, name + "_接诊记录", "接诊记录", DoctorRecordExportVO.class, exportData);
            log.info("导出医生接诊记录成功：doctorId={}, 记录数={}", doctorId, exportData.size());
        } catch (IOException e) {
            log.error("导出接诊记录失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出预约记录，支持按科室、日期范围筛选
     */
    @Override
    public void exportAppointments(Long departmentId, String startDate, String endDate, HttpServletResponse response) {
        try {
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            if (departmentId != null) {
                List<Long> doctorIds = getDoctorIdsByDepartment(departmentId);
                if (doctorIds.isEmpty()) {
                    writeExcel(response, "预约记录", "预约记录", AppointmentExportVO.class, new ArrayList<>());
                    return;
                }
                wrapper.in(Appointment::getDoctorId, doctorIds);
            }
            if (startDate != null && !startDate.isBlank()) {
                wrapper.ge(Appointment::getAppointmentDate, parseDate(startDate));
            }
            if (endDate != null && !endDate.isBlank()) {
                wrapper.le(Appointment::getAppointmentDate, parseDate(endDate));
            }
            wrapper.orderByDesc(Appointment::getAppointmentDate, Appointment::getId);

            List<Appointment> appointments = appointmentMapper.selectList(wrapper);
            List<AppointmentExportVO> exportData = new ArrayList<>();
            for (Appointment apt : appointments) {
                AppointmentExportVO vo = new AppointmentExportVO();
                vo.setAppointmentNo(toText(apt.getAppointmentNo()));
                vo.setAppointmentDate(toText(apt.getAppointmentDate()));
                vo.setTimeSlot(toText(apt.getTimeSlot()));
                vo.setStatus(getStatusText(apt.getStatus()));
                vo.setAmount(apt.getAmount());
                vo.setCreateTime(toText(apt.getCreateTime()));
                exportData.add(vo);
            }

            writeExcel(response, "预约记录", "预约记录", AppointmentExportVO.class, exportData);
            log.info("导出预约记录成功：departmentId={}, 记录数={}", departmentId, exportData.size());
        } catch (IOException e) {
            log.error("导出预约记录失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出科室统计报表：统计每个科室在时间范围内的预约数
     */
    @Override
    public void exportDepartmentStatistics(String startDate, String endDate, HttpServletResponse response) {
        try {
            List<Department> departments = departmentMapper.selectList(null);
            List<DepartmentStatExportVO> exportData = new ArrayList<>();

            for (Department dept : departments) {
                DepartmentStatExportVO vo = new DepartmentStatExportVO();
                vo.setDepartmentName(dept.getName());
                vo.setDepartmentCode(dept.getCode());
                vo.setLocation(dept.getLocation());
                vo.setPhone(dept.getPhone());

                List<Long> doctorIds = getDoctorIdsByDepartment(dept.getId());
                long count = 0;
                if (!doctorIds.isEmpty()) {
                    LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(Appointment::getDoctorId, doctorIds);
                    if (startDate != null && !startDate.isBlank()) {
                        wrapper.ge(Appointment::getAppointmentDate, parseDate(startDate));
                    }
                    if (endDate != null && !endDate.isBlank()) {
                        wrapper.le(Appointment::getAppointmentDate, parseDate(endDate));
                    }
                    count = appointmentMapper.selectCount(wrapper);
                }
                vo.setAppointmentCount((int) count);
                exportData.add(vo);
            }

            writeExcel(response, "科室统计报表", "科室统计", DepartmentStatExportVO.class, exportData);
            log.info("导出科室统计成功：科室数={}", exportData.size());
        } catch (IOException e) {
            log.error("导出科室统计失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    // ============================================================
    // 私有辅助方法
    // ============================================================

    /**
     * 查询科室下的医生ID列表，用于按科室筛选预约
     */
    private List<Long> getDoctorIdsByDepartment(Long departmentId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDepartmentId, departmentId);
        return doctorMapper.selectList(wrapper).stream().map(Doctor::getId).toList();
    }

    /**
     * 统一写出Excel：设置响应头（UTF-8文件名）并写入数据
     */
    private void writeExcel(HttpServletResponse response, String baseFileName, String sheetName,
                            Class<?> clazz, List<?> data) throws IOException {
        String fileName = URLEncoder.encode(baseFileName, StandardCharsets.UTF_8).replace("+", "%20") + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
    }

    /**
     * 解析 yyyy-MM-dd 格式日期，非法时给出友好提示
     */
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误，应为 yyyy-MM-dd：" + date);
        }
    }

    /**
     * 空值转空字符串，避免导出时出现 null
     */
    private String toText(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * 将状态码转换为中文文本
     */
    private String getStatusText(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "pending": return "待支付";
            case "paid": return "已支付";
            case "cancelled": return "已取消";
            case "completed": return "已完成";
            default: return status;
        }
    }
}

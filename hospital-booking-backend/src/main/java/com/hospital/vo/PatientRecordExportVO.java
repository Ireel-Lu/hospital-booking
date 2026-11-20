package com.hospital.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 患者就诊记录导出VO类
 * 用于Excel导出患者就诊记录时的数据映射
 */
@Data
@ColumnWidth(20)
public class PatientRecordExportVO {

    @ExcelProperty("预约单号")
    private String appointmentNo;      // 预约单号

    @ExcelProperty("医生姓名")
    private String doctorName;         // 医生姓名

    @ExcelProperty("医生职称")
    private String doctorTitle;        // 医生职称

    @ExcelProperty("科室")
    private String departmentName;     // 科室名称

    @ExcelProperty("就诊日期")
    private String appointmentDate;    // 就诊日期

    @ExcelProperty("时段")
    private String timeSlot;           // 就诊时段

    @ExcelProperty("症状")
    private String symptoms;           // 症状描述

    @ExcelProperty("诊断结果")
    private String diagnosis;          // 诊断结果

    @ExcelProperty("医嘱")
    private String advice;             // 医嘱

    @ExcelProperty("就诊时间")
    private String visitTime;          // 实际就诊时间
}

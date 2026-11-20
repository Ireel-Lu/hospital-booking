package com.hospital.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 医生就诊记录导出VO类
 * 用于Excel导出医生接诊记录时的数据映射
 */
@Data
@ColumnWidth(20)
public class DoctorRecordExportVO {

    @ExcelProperty("患者姓名")
    private String patientName;        // 患者姓名

    @ExcelProperty("患者电话")
    private String patientPhone;       // 患者电话

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

package com.hospital.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 预约记录导出VO类
 * 用于Excel导出预约数据时的数据映射
 */
@Data
@ColumnWidth(20)
public class AppointmentExportVO {

    @ExcelProperty("预约单号")
    private String appointmentNo;      // 预约单号

    @ExcelProperty("预约日期")
    private String appointmentDate;    // 预约日期

    @ExcelProperty("时段")
    private String timeSlot;           // 预约时段

    @ExcelProperty("状态")
    private String status;             // 预约状态

    @ExcelProperty("金额")
    private BigDecimal amount;         // 预约金额

    @ExcelProperty("创建时间")
    private String createTime;         // 创建时间
}

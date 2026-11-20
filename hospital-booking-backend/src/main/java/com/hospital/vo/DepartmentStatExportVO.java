package com.hospital.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 科室统计导出VO类
 * 用于Excel导出科室统计数据时的数据映射
 */
@Data
@ColumnWidth(20)
public class DepartmentStatExportVO {

    @ExcelProperty("科室名称")
    private String departmentName;     // 科室名称

    @ExcelProperty("科室编码")
    private String departmentCode;     // 科室编码

    @ExcelProperty("位置")
    private String location;           // 科室位置

    @ExcelProperty("电话")
    private String phone;              // 科室联系电话

    @ExcelProperty("预约数")
    private Integer appointmentCount;  // 预约数量
}

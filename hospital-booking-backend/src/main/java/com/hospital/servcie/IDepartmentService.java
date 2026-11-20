package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.entity.Department;

import java.util.List;
import java.util.Map;

public interface IDepartmentService extends IService<Department> {
    List<Map<String,Object>> getDepartmentList(String keyword);
}

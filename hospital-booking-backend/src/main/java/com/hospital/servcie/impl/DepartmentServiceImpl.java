package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.entity.Department;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.servcie.IDepartmentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {
    @Override
    public List<Map<String,Object>> getDepartmentList(String keyword) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if(keyword !=null && !keyword.trim().isEmpty()){
            wrapper.like(Department::getName,keyword)
                    .or()
                    .like(Department::getCode,keyword);
        }

        List<Department> departments = list(wrapper);
        return convertToList(departments);
    }

    /**
     * 转换对象为map格式
     */
    private List<Map<String,Object>> convertToList(List<Department> departments){
        List<Map<String,Object>> result = new ArrayList<>();
        for(Department d : departments){
            //使用HashMap而不是Map.of：description/location/phone 等字段可能为null
            Map<String,Object> item = new java.util.HashMap<>();
            item.put("id",d.getId());
            item.put("name",d.getName());
            item.put("code",d.getCode());
            item.put("description",d.getDescription());
            item.put("location",d.getLocation());
            item.put("phone",d.getPhone());
            item.put("sortOrder",d.getSortOrder());
            item.put("status",d.getStatus());
            result.add(item);
        }

        return result;
    }
}

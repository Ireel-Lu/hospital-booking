package com.hospital.booking.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.booking.entity.Department;
import com.hospital.booking.entity.Doctor;
import com.hospital.booking.mapper.DepartmentMapper;
import com.hospital.booking.mapper.DoctorMapper;
import com.hospital.booking.servcie.IDoctorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements IDoctorService {

    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(DepartmentMapper departmentMapper, DoctorMapper doctorMapper) {
        this.departmentMapper = departmentMapper;
        this.doctorMapper = doctorMapper;
    }

    @Override
    public List<Map<String,Object>> getDoctors(Long departmentId){
        return getDoctors(departmentId,null);
    }

    @Override
    public List<Map<String,Object>> getDoctors(Long departmentId, String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();

        if(departmentId != null){
            wrapper.eq(Doctor::getDepartmentId,departmentId);
        }

        if(keyword != null && !keyword.trim().isEmpty()){
            wrapper.like(Doctor::getName,keyword)
                    .or()
                    .like(Doctor::getTitle,keyword)
                    .or()
                    .like(Doctor::getSpecialty,keyword);
        }

        wrapper.eq(Doctor::getStatus,1);

        List<Doctor> doctors = this.list(wrapper);

        return doctors.stream().map(doctor -> {
            Map<String,Object> map = new HashMap<>();
            map.put("id",doctor.getId());
            map.put("name",doctor.getName());
            map.put("title", doctor.getTitle());
            map.put("specialty", doctor.getSpecialty());
            map.put("introduction", doctor.getIntroduction());
            map.put("consultationFee", doctor.getConsultationFee());
            map.put("avatar", doctor.getAvatar());

            //科室名称
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            map.put("departmentName",department.getName());

            return map;
        }).collect(Collectors.toList());

    }

    @Override
    public List<Map<String, Object>> searchDoctors(String keyword) {
        return doctorMapper.searchDoctors(keyword);
    }

    @Override
    public List<Map<String, Object>> getAllDoctors() {
        return getDoctors(null);
    }

    @Override
    public Map<String, Object> getSchedule(Long doctorId, LocalDate date) {
        Map<String,Object> result = new HashMap<>();
        result.put("doctorId",doctorId);
        result.put("date",date.toString());

        List<Map<String,Object>> timeSlots = new ArrayList<>();
        String[] slots = {
                "08:00-09:00", "09:00-10:00", "10:00-11:00",
                "11:00-12:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"
        };

        for(String slot : slots){
            Map<String,Object> slotMap = new HashMap<>();
            slotMap.put("timeSlot",slot);
            slotMap.put("available",true);
            timeSlots.add(slotMap);
        }

        result.put("timeSlots",timeSlots);


        return result;
    }

    @Override
    public List<Map<String, Object>> getPopularDoctors(int limit) {
        return doctorMapper.getPopularDoctors(limit);
    }


}

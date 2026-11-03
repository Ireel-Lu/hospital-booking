package com.hospital.booking.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.booking.entity.Doctor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IDoctorService extends IService<Doctor> {

    /**
     * 根据科室医生列表
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<Map<String, Object>> getDoctors(Long departmentId);

    /**
     * 获取医生列表（支持搜索）
     * @param departmentId 科室ID（可选）
     * @param keyword 搜索关键字（匹配姓名、职称、专长）
     * @return 医生列表
     */
    List<Map<String, Object>> getDoctors(Long departmentId, String keyword);

    /**
     * 根据关键字搜索医生
     * @param keyword 搜索关键字
     * @return 匹配的医生列表
     */
    List<Map<String, Object>> searchDoctors(String keyword);

    /**
     * 获取所有医生
     * @return 所有医生列表
     */
    List<Map<String, Object>> getAllDoctors();

    /**
     * 获取医生排班信息
     * @param doctorId 医生ID
     * @param date 日期
     * @return 排班信息
     */
    Map<String, Object> getSchedule(Long doctorId, LocalDate date);

    /**
     * 获取热门医生
     * @param limit 返回数量限制
     * @return 热门医生列表
     */
    List<Map<String, Object>> getPopularDoctors(int limit);

}

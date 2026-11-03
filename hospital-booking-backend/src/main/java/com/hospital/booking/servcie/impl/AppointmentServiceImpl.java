package com.hospital.booking.servcie.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.booking.dto.AppointmentDTO;
import com.hospital.booking.entity.*;
import com.hospital.booking.mapper.*;
import com.hospital.booking.servcie.IAppointmentService;
import com.hospital.booking.servcie.IOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.Source;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements IAppointmentService {

    private final AppointmentMapper appointmentMapper;

    private final SourceSettingMapper sourceSettingMapper;

    private final DoctorMapper doctorMapper;

    private final PatientQueueMapper patientQueueMapper;

    private final DepartmentMapper departmentMapper;

    private final IOperationLogService operationLogService;

    private final HttpServletRequest request;


    /**
     * 新增预约
     */
    @Override
    @Transactional
    public Map<String, Object> createAppointment(AppointmentDTO dto) {
        Map<String, Object> result = new HashMap<>();

        //1.检查号源是否充足
        SourceSetting sourceSetting = sourceSettingMapper.selectById(dto.getSourceSettingId());
        if(sourceSetting == null || sourceSetting.getRemainingQuantity()<=0){
            result.put("sucess",false);
            result.put("message","号源已满");
            return result;
        }

        //2.检查同一时间段是否重复预约
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId,dto.getPatientId())
                .eq(Appointment::getAppointmentDate,sourceSetting.getDate())
                .eq(Appointment::getTimeSlot,sourceSetting.getTimeSlot())
                .eq(Appointment::getStatus,"cacelled");
        Long count = this.count(wrapper);
        if(count>0){
            result.put("sucess",false);
            result.put("message","同一时间段已预约,请勿重复预约或者选择其他时间段");
            return result;
        }

        //3.创建预约记录
        Appointment appointment = new Appointment();
        // 生成唯一预约单号（使用Hutool的fastSimpleUUID，生成32位不含横线的UUID，转大写便于阅读）
        appointment.setAppointmentNo(IdUtil.fastSimpleUUID().toUpperCase());
        appointment.setPatientId(dto.getPatientId());
        appointment.setDoctorId(dto.getDoctorId());
        appointment.setSourceSettingId(dto.getSourceSettingId());
        appointment.setAppointmentDate(sourceSetting.getDate());            // 预约日期
        appointment.setTimeSlot(sourceSetting.getTimeSlot());               // 预约时段
        appointment.setSymptoms(dto.getSymptoms());                         // 症状描述（患者主诉）
        appointment.setStatus("pending");                                   // 状态：待支付（初始状态）
        appointment.setAmount(getConsultationFee(dto.getDoctorId()));       // 挂号费（从医生信息中获取）
        this.save(appointment);

        //4.扣减号源
        sourceSetting.setRemainingQuantity(sourceSetting.getRemainingQuantity() - 1);
        sourceSettingMapper.updateById(sourceSetting);

        //5.记录操作日志
        operationLogService.log(
                dto.getPatientId(),
                "CREATE_APPOINTMENT",
                "Appointment",
                appointment.getId(),
                "创建预约："+appointment.getAppointmentNo(),
                getIpAddress()
        );

        //6.返回成功结果
        result.put("success",true);
        result.put("appointment",appointment);

        return result;
    }

    /**
     * 支付预约
     */
    @Override
    @Transactional
    public boolean payAppointment(Long id) {
        //1.查询预约并校检状态
        Appointment appointment = this.getById(id);
        if(appointment == null || "pending".equals(appointment.getStatus())){
            return false;
        }

        //2.更新预约状态为已支付
        appointment.setStatus("paid");
        appointment.setPayTime(LocalDateTime.now());
        this.updateById(appointment);

        //3.生成排队号
        Integer queueNumber = generateQueueNumber(appointment.getDoctorId(), appointment.getAppointmentDate());
        appointment.setQueueNumber(queueNumber);
        this.updateById(appointment);

        //4.加入候诊队列
        PatientQueue queue =new PatientQueue();
        queue.setAppointmentId(id);
        queue.setQueueNumber(queueNumber);
        queue.setDoctorId(appointment.getDoctorId());
        queue.setStatus("waiting");
        queue.setCreateTime(LocalDateTime.now());
        patientQueueMapper.insert(queue);

        //5.记录操作日志
        operationLogService.log(
                appointment.getPatientId(),
                "PAY_APPOINTMENT",
                "Appointment",
                id,
                "支付预约："+appointment.getAppointmentNo(),
                getIpAddress()
        );

        return true;
    }

    /**
     * 取消预约
     */
    @Override
    public boolean cancelAppointment(Long id, String reason) {
        //1.查询预约并校检状态
        Appointment appointment = this.getById(id);
        if(appointment == null || !"paid".equals(appointment.getStatus())){
            return false;
        }

        //2.更新预约状态为已取消
        appointment.setStatus("cancelled");
        appointment.setCancelTime(LocalDateTime.now());
        this.updateById(appointment);

        //3.释放号源
        SourceSetting sourceSetting = sourceSettingMapper.selectById(appointment.getSourceSettingId());
        if(sourceSetting != null){
            sourceSetting.setRemainingQuantity(sourceSetting.getRemainingQuantity() + 1);
            sourceSettingMapper.updateById(sourceSetting);
        }

        //4.更新队列状态
        LambdaQueryWrapper<PatientQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientQueue::getAppointmentId, id);
        PatientQueue queue = patientQueueMapper.selectOne(wrapper);
        if(queue != null){
            // 将队列状态更新为"skipped"
            // 队列状态流转：waiting → calling → visited
            // 或 waiting → skipped
            queue.setStatus("skipped");
            patientQueueMapper.updateById(queue);
        }

        //5.记录操作日志
        operationLogService.log(
                appointment.getPatientId(),
                "CANCEL_APPOINTMENT",
                "Appointment",
                id,
                "取消预约："+appointment.getAppointmentNo()+"，原因："+reason,
                getIpAddress()
        );

        //返回取消成功
        return true;
    }

    /**
     * 获取用户的预约列表
     */
    @Override
    public List<Map<String,Object>> getUserAppointments(Long userId) {
        List<Map<String,Object>> result = new ArrayList<>();

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, userId)
                .orderByDesc(Appointment::getAppointmentDate);
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);

        if(appointments == null || appointments.isEmpty()){
            return result;
        }

        //预约列表应该包括预约详情、医生、科室等
        for(Appointment apt : appointments){
            Map<String,Object> item = new HashMap<>();
            item.put("id",apt.getId());
            item.put("appointmentNo",apt.getAppointmentNo());
            item.put("timeSlot",apt.getTimeSlot());
            item.put("symptoms", apt.getSymptoms());
            item.put("status", apt.getStatus());
            item.put("queueNumber", apt.getQueueNumber());
            item.put("amount", apt.getAmount());
            item.put("createTime", apt.getCreateTime());
            item.put("payTime", apt.getPayTime());
            item.put("cancelTime", apt.getCancelTime());

            Doctor doctor = doctorMapper.selectById(apt.getDoctorId());
            if(doctor != null){
                item.put("doctorName", doctor.getName());
                item.put("doctorTitle", doctor.getTitle());
                item.put("doctorId", doctor.getId());

                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                if(department != null){
                    item.put("departmentName", department.getName());
                    item.put("departmentId", department.getId());
                }
            }
            result.add(item);
        }

        return result;
    }

    /**
     * 根据条件筛选预约
     */
    @Override
    public List<Map<String,Object>> filterAppointments(Map<String, Object> params) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();

        if(params.get("patientId") != null){
            wrapper.eq(Appointment::getPatientId,Long.parseLong(params.get("patientId").toString()));
        }

        if(params.get("doctorId") != null){
            wrapper.eq(Appointment::getDoctorId,Long.parseLong((params.get("doctorId").toString())));
        }

        if(params.get("status") != null && "all".equals(params.get("status"))){
            wrapper.eq(Appointment::getStatus,params.get("status").toString());
        }

        if(params.get("startDate") != null){
            // 大于等于开始日期
            wrapper.ge(Appointment::getAppointmentDate,LocalDate.parse(params.get("startDate").toString()));
        }

        if(params.get("endDate") != null){
            // 小于等于结束日期
            wrapper.le(Appointment::getAppointmentDate,LocalDate.parse(params.get("endDate").toString()));
        }

        wrapper.orderByDesc(Appointment::getCreateTime);

        List<Appointment> appointments = this.list(wrapper);
        List<Map<String,Object>> result = new ArrayList<>();

        for(Appointment apt : appointments){
            //预约信息
            Map<String,Object> item = new HashMap<>();
            item.put("id", apt.getId());
            item.put("appointmentNo", apt.getAppointmentNo());
            item.put("appointmentDate", apt.getAppointmentDate());
            item.put("timeSlot", apt.getTimeSlot());
            item.put("status", apt.getStatus());
            item.put("amount", apt.getAmount());
            item.put("createTime", apt.getCreateTime());

            //获取医生信息
            Doctor doctor = doctorMapper.selectById(apt.getDoctorId());
            if(doctor !=null ){
                item.put("doctorName", doctor.getName());
                item.put("doctorTitle", doctor.getTitle());

                //获取科室信息
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                if(department != null){
                    item.put("departmentName", department.getName());
                    item.put("departmentId", department.getId());
                }
            }
            result.add(item);
        }

        return result;
    }


    /**
     * 获取医生候诊列表
     */


    /**
     * 叫号
     */


    /**
     * 获取队列实时状态
     */


    /**
     * 获取用户的预约队列
     */



    // ==================================
    // 私有辅助方法
    // ==================================

    /**
     * 获取医生挂号费
     *
     * 挂号费由医生的职称决定：
     * - 主任医师：挂号费较高
     * - 副主任医师：中等
     * - 主治医师：较低
     * 具体金额在医生信息中维护
     *
     * @param doctorId 医生ID
     * @return 挂号费金额，如果医生不存在则返回0
     */
    private BigDecimal getConsultationFee(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        return doctor != null ? doctor.getConsultationFee() : BigDecimal.ZERO;
    }

    /**
     * 获取客户端IP地址
     * 从HTTP请求头中获取真实IP
     * 支持代理环境（X-Forwarded-For）
     *
     * 请求头优先级：
     * 1. X-Forwarded-For：代理服务器转发的真实IP
     * 2. 如果没有代理，直接获取 RemoteAddr
     *
     * 注意：
     * - X-Forwarded-For可能包含多个IP（逗号分隔），取第一个为客户端真实IP
     * - 在本地开发环境中，可能返回 "0:0:0:0:0:0:0:1"（IPv6的localhost）
     *
     * @return 客户端IP地址，如果无法获取则返回空字符串
     */
    private String getIpAddress() {
        if (request == null) return "";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }

    /**
     * 生成排队号
     *
     * 按照医生和日期分组，从1开始递增
     * 计算公式：该医生该日期已支付数量+1
     *
     * 示例：
     * 该医生今天第一个支付的患者：排队号1
     * 该医生今天第七个支付的患者：排队号5
     *
     * 并发安全：
     * 在事务中执行，通过数据库锁保证排队号的唯一性
     * count查询和insert操作在同一事务中，不会出现重复号
     *
     * @param doctorId 医生ID
     * @param date 日期
     * @return 排队号（从1开始，最小为1）
     */
    private Integer generateQueueNumber(Long doctorId, LocalDate date){
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId , doctorId)
                .eq(Appointment::getAppointmentDate , date)
                .eq(Appointment::getStatus , "paid");

        long count = this.count(wrapper);

        return (int) count+1;
    }
}

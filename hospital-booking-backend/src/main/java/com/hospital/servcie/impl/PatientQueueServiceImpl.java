package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.PatientQueue;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.PatientQueueMapper;
import com.hospital.webSocket.CallWebSocket;
import com.hospital.servcie.IPatientQueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class PatientQueueServiceImpl extends ServiceImpl<PatientQueueMapper, PatientQueue> implements IPatientQueueService {

    private final PatientQueueMapper patientQueueMapper;
    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;

    @Override
    public List<Map<String, Object>> getQueueStatus(Long doctorId){
        return patientQueueMapper.getWaitingQueue(doctorId);
    }

    @Override
    @Transactional
    public void skipCurrentPatient(Long queueId) {
        PatientQueue queue = patientQueueMapper.selectById(queueId);
        if(queue == null){
            throw new RuntimeException("队列不存在");
        }

        queue.setStatus("skipped");
        patientQueueMapper.updateById(queue);

        Appointment appointment = appointmentMapper.selectById(queue.getAppointmentId());
        if(appointment != null){
            appointment.setStatus("cancelled");
            appointmentMapper.updateById(appointment);
        }

        log.info("已跳过队列id={}的患者，相关预约id={}已取消", queueId, queue.getAppointmentId());

    }

    @Override
    public Map<String,Object> callNextPatient(Long doctorId) {
        Map<String,Object> result = new HashMap<>();

        try{//检测当前是否有患者正在就诊，以免重复呼叫
            Map<String, Object> callingPatient = patientQueueMapper.getCallingPatientByDoctorId(doctorId);

            if (callingPatient != null && !callingPatient.isEmpty()) {
                result.put("status", false);
                result.put("message", "当前有患者正在叫号中，请先完成该次就诊");
                return result;
            }

            //检测叫号的患者是否存在
            Map<String, Object> nextPatient = patientQueueMapper.getNextPatientByDoctorId(doctorId);

            if (nextPatient == null) {
                result.put("status", false);
                result.put("message", "当前没有等待的患者");
                return result;
            }

            //检测叫号患者的状态
            Long QueueId = nextPatient.get("id") != null ? ((Number) nextPatient.get("id")).longValue() : null;
            Integer queueNumber = (Integer) nextPatient.get("queue_number");
            String patientName = (String) nextPatient.get("patient_name");
            Long appointmentId = nextPatient.get("appointment_id") != null ? ((Number) nextPatient.get("appointment_id")).longValue() : null;

            if (QueueId == null || appointmentId == null) {
                result.put("status", false);
                result.put("message", "患者信息不完整，无法呼叫");
                return result;
            }

            patientQueueMapper.updateStatus(QueueId, "calling");
            Appointment appointment = appointmentMapper.selectById(appointmentId);
            if (appointment != null) {
                appointment.setStatus("calling");
                appointmentMapper.updateById(appointment);
            }

            //查询下一个等待的患者的信息
            Integer nextNumber = null;
            String nextPatientName = null;
            Map<String, Object> nextWaitingPatient = patientQueueMapper.getNextPatient(doctorId, queueNumber);
            if (nextWaitingPatient != null) {
                nextNumber = (Integer) nextWaitingPatient.get("queue_number");
                nextPatientName = (String) nextWaitingPatient.get("patient_name");
            }

            //统计当前队列中等待的患者数量
            Integer waitingCount = patientQueueMapper.countWaitingPatients(doctorId);

            try {
                CallWebSocket.sendCallNotification(
                        String.valueOf(doctorId), queueNumber, patientName, nextNumber, nextPatientName, waitingCount);
                CallWebSocket.refreshQueueDisplay();
            } catch (Exception e) {
                log.warn("WebSocket发送呼叫通知失败，不影响叫号流程", e);
            }

            result.put("success", true);
            result.put("queueId", QueueId);
            result.put("queueNumber", queueNumber);
            result.put("currentNumber", queueNumber);
            result.put("currentPatient", patientName);
            result.put("patientName", patientName);
            result.put("appointmentId", appointmentId);

            result.put("nextNumber", nextNumber);
            result.put("waitingCount", waitingCount);

            log.info("医生id={}呼叫下一位患者：当前队列号={}, 患者姓名={}, 下一号={}, 等候={}", doctorId, queueNumber, patientName, nextNumber, waitingCount);
        }catch(Exception e){
            log.error("呼叫下一位患者失败，医生ID={}",doctorId, e);
            result.put("status", false);
            result.put("message", "呼叫下一位患者失败,请重试");
        }
        return result;
    }

    @Override
    @Transactional
    public void startVisit(Long queueId) {
        PatientQueue queue = patientQueueMapper.selectById(queueId);
        if (queue == null) {
            throw new RuntimeException("队列不存在");
        }

        queue.setStatus("visited");
        queue.setVisitTime(LocalDateTime.now());
        patientQueueMapper.updateById(queue);

        // 更新预约状态
        Appointment appointment = appointmentMapper.selectById(queue.getAppointmentId());
        if (appointment != null) {
            appointment.setStatus("completed");
            appointmentMapper.updateById(appointment);
        }

        log.info("开始就诊：queueId={}", queueId);
    }

    @Override
    @Transactional
    public void completeVisit(Long queueId) {
        PatientQueue queue = patientQueueMapper.selectById(queueId);
        if (queue == null) {
            throw new RuntimeException("该队列号不存在");
        }

        queue.setStatus("completed");
        patientQueueMapper.updateById(queue);

        log.info("完成就诊：queueId={}", queueId);
    }

    @Override
    public List<Map<String, Object>> getQueueStatusByUserId(Long userId) {
        Long doctorId = getDoctorIdByUserId(userId);
        Doctor doctor = doctorMapper.selectById(doctorId);
        log.info("当前医生:id={},名字={}的候诊队列：", doctorId, doctor == null ? null : doctor.getName());
        return getQueueStatus(doctorId);
    }

    @Override
    public Map<String,Object> callNextPatientByUserId(Long userId) {
        Long doctorId = getDoctorIdByUserId(userId);
        Doctor doctor = doctorMapper.selectById(doctorId);
        log.info("医生id={}，名字={}，呼叫下一位患者", doctorId, doctor == null ? null : doctor.getName());
        return callNextPatient(doctorId);
    }

    /**
     * 患者侧：查询自己今天的候诊进度
     */
    @Override
    public Map<String,Object> getPatientQueueStatusByUserId(Long userId) {
        Map<String, Object> status = patientQueueMapper.getPatientQueueStatus(userId);
        if (status == null) {
            status = new HashMap<>();
        }
        status.put("hasQueue", !status.isEmpty());
        return status;
    }

    /**
     * 根据用户ID获取对应医生ID
     */
    private Long getDoctorIdByUserId(Long userId) {
        Long doctorId = doctorMapper.getDoctorIdByUserId(userId);
        if (doctorId == null) {
            throw new RuntimeException("当前用户不是医生");
        }
        return doctorId;
    }

    @Override
    @Transactional
    public void addToQueue(Long appointmentId, Integer queueNumber) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("该预约不存在");
        }

        // 检查是否已在队列中
        LambdaQueryWrapper<PatientQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientQueue::getAppointmentId, appointmentId);
        if (this.count(wrapper) > 0) {
            log.warn("预约已在队列中：appointmentId={}", appointmentId);
            return;
        }

        PatientQueue queue = new PatientQueue();
        queue.setAppointmentId(appointmentId);
        queue.setDoctorId(appointment.getDoctorId());
        queue.setQueueNumber(queueNumber);
        queue.setStatus("waiting");
        queue.setCreateTime(LocalDateTime.now());

        this.save(queue);

        log.info("已添加到队列：appointmentId={}, queueNumber={}", appointmentId, queueNumber);
    }
}

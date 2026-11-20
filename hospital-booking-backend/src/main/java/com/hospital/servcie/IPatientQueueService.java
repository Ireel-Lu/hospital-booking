package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.entity.PatientQueue;

import java.util.List;
import java.util.Map;

public interface IPatientQueueService extends IService<PatientQueue> {
    List<Map<String, Object>> getQueueStatus(Long doctorId);

    void skipCurrentPatient(Long queueId);

    Map<String,Object> callNextPatient(Long doctorId);

    void startVisit(Long queueId);

    void completeVisit(Long queueId);

    void addToQueue(Long appointmentId, Integer queueNumber);

    /**
     * 根据用户ID获取该医生当前的队列状态
     * @param userId 用户ID
     * @return 候诊队列列表
     */
    List<Map<String, Object>> getQueueStatusByUserId(Long userId);

    /**
     * 根据用户ID呼叫下一位患者
     * @param userId 用户ID
     * @return 叫号结果
     */
    Map<String,Object> callNextPatientByUserId(Long userId);

    /**
     * 患者侧：查询自己今天的候诊进度
     * @param userId 患者用户ID
     * @return 当前叫号、我的号、前面等待人数等
     */
    Map<String,Object> getPatientQueueStatusByUserId(Long userId);
}

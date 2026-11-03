package com.hospital.booking.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.booking.entity.OperationLog;

public interface IOperationLogService extends IService<OperationLog> {

    void log(Long operatorId, String operationType, String targetType, Long targetId, String content, String ipAddress);
}

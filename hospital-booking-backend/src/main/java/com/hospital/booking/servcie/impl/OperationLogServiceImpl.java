package com.hospital.booking.servcie.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.booking.entity.OperationLog;
import com.hospital.booking.mapper.OperationLogMapper;

import com.hospital.booking.servcie.IOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper,OperationLog> implements IOperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public void log(Long operatorId, String operationType, String targetType, Long targetId, String content, String ipAddress) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setContent(content);
        log.setIpAddress(ipAddress);

        this.save(log);
    }
}

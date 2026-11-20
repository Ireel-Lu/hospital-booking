package com.hospital.servcie.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hospital.entity.OperationLog;
import com.hospital.mapper.OperationLogMapper;

import com.hospital.servcie.IOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<OperationLog> listLogs() {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return this.list(wrapper);
    }
}

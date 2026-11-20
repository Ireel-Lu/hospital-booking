package com.hospital.servcie;

import com.baomidou.mybatisplus.spring.service.IService;
import com.hospital.entity.OperationLog;

import java.util.List;

public interface IOperationLogService extends IService<OperationLog> {

    void log(Long operatorId, String operationType, String targetType, Long targetId, String content, String ipAddress);

    /**
     * 查询操作日志列表（按创建时间倒序）
     * @return 操作日志列表
     */
    List<OperationLog> listLogs();
}

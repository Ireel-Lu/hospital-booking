package com.hospital.controller;

import com.hospital.entity.Result;
import com.hospital.servcie.IOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作日志", description = "LogController")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final IOperationLogService operationLogService;

    /**
     * 查询操作日志列表
     * 按创建时间倒序排列，最新的日志排在前面
     * @return 操作日志列表
     */
    @Operation(summary = "查询日志列表（list）")
    @GetMapping("/list")
    public Result list() {
        return Result.success(operationLogService.listLogs());
    }

}

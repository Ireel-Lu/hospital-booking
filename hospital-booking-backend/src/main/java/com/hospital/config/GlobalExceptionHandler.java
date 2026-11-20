package com.hospital.config;

import com.hospital.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * 把业务代码里抛出的 RuntimeException 统一转换成 Result，
 * 避免前端只收到一个没有 message 的 500 响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 角色不匹配：交给前端提示"无权限"，而不是当成登录过期
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, "无权限访问该接口"));
    }

    /**
     * @Valid 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(400, message);
    }

    /**
     * 业务异常（用户不存在、密码错误、号源已满等）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage() == null ? "操作失败" : e.getMessage());
    }
}

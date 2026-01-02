package com.example.oldcaresystem.exception;

import com.example.oldcaresystem.util.ResponseUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseUtil<String> handleBusinessException(BusinessException e) {
        return ResponseUtil.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseUtil<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseUtil.error("系统异常，请稍后重试");
    }
}

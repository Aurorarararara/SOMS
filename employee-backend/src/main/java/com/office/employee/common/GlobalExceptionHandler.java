package com.office.employee.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * 全局异常处理器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append("; ");
        }
        
        logger.warn("参数校验失败: {}", errorMessage.toString());
        return Result.error(400, errorMessage.toString());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        logger.error("系统异常: ", e);
        return Result.error("系统异常，请联系管理员");
    }
}
package edu.hebeu.partnermatching.exception;

import edu.hebeu.partnermatching.common.BaseResponse;
import edu.hebeu.partnermatching.common.ErrorCode;
import edu.hebeu.partnermatching.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException exception){
        log.error("businessException" + exception.getMessage(),exception);
        return ResultUtils.error(exception.getCode(),exception.getMessage(),exception.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(BusinessException exception){
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,exception.getMessage());
    }
}

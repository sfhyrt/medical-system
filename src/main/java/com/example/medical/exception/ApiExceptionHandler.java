package com.example.medical.exception;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import java.util.stream.Collectors;
@RestControllerAdvice
public class ApiExceptionHandler {
 public record ApiError(String code,String message){}
 private ResponseEntity<ApiError> error(HttpStatus status,String code,String message){return ResponseEntity.status(status).body(new ApiError(code,message));}
 @ExceptionHandler(ResourceNotFoundException.class) public ResponseEntity<ApiError> missing(ResourceNotFoundException e){return error(HttpStatus.NOT_FOUND,"NOT_FOUND",e.getMessage());}
 @ExceptionHandler(InvalidReferenceException.class) public ResponseEntity<ApiError> reference(InvalidReferenceException e){return error(HttpStatus.BAD_REQUEST,"INVALID_REFERENCE",e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ApiError> fields(MethodArgumentNotValidException e){
  return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).sorted().collect(Collectors.joining("；")));
 }
 @ExceptionHandler({ConstraintViolationException.class,HandlerMethodValidationException.class,MethodArgumentTypeMismatchException.class,HttpMessageNotReadableException.class})
 public ResponseEntity<ApiError> malformed(Exception e){return error(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","请求参数格式或范围不正确，请检查分页、编号及表单内容");}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ApiError> conflict(DataIntegrityViolationException e){return error(HttpStatus.CONFLICT,"DATA_CONFLICT","编码、名称或身份证已存在，或该记录已被关联数据引用");}
 @ExceptionHandler(DataAccessException.class) public ResponseEntity<ApiError> database(DataAccessException e){return error(HttpStatus.SERVICE_UNAVAILABLE,"DATABASE_UNAVAILABLE","数据服务暂不可用，请稍后重试");}
}

package br.com.tarefa.exceptions.handlers;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.tarefa.exceptions.AuthorizationException;
import br.com.tarefa.exceptions.BusinessException;
import br.com.tarefa.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
	
    @ExceptionHandler(value = {AuthorizationException.class})
    public ResponseEntity<ApiRequestException> haddlerAuthorizationException(AuthorizationException e, HttpServletRequest request) {

        ApiRequestException apiException = ApiRequestException.builder()
                .title("Forbidden")
                .message(e.getMessage())
                .httpStatus(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now()).build();

        this.logError(HttpStatus.FORBIDDEN, e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiException);
    }
	
    @ExceptionHandler(value = {BusinessException.class})
    public ResponseEntity<ApiRequestException> haddlerBusinessException(BusinessException e, HttpServletRequest request) {

        ApiRequestException apiException = ApiRequestException.builder()
                .title("Bad Request")
                .message(e.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now()).build();

        this.logError(HttpStatus.BAD_REQUEST, e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiException);
    }
    
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<ApiRequestException> haddlerConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {

        ApiRequestException apiException = ApiRequestException.builder()
                .title("Bad Request")
                .message(e.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now()).build();

        this.logError(HttpStatus.BAD_REQUEST, e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiException);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<ApiRequestException> haddlerHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {

        ApiRequestException apiException = ApiRequestException.builder()
                .title("Bad Request")
                .message(e.getCause().toString())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now()).build();

        this.logError(HttpStatus.BAD_REQUEST, e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiException);
    }
    
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ApiRequestException> haddlerResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {

        ApiRequestException apiException = ApiRequestException.builder()
                .title("Resource Not Found")
                .message(e.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now()).build();

        this.logError(HttpStatus.NOT_FOUND, e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiException);
    }
    
    private void logError(HttpStatus status, String msg) {
        log.error("Status Http: {}, message: {}\n", status.value(), msg);
    }
    
}

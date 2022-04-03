package com.master.domain_invoice_service.controller;

import com.master.domain_invoice_service.dto.ErrorDto;
import com.master.domain_invoice_service.exception.ObjectNotFoundException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ErrorDto> handleObjectNotFoundException(
        ObjectNotFoundException exception) {
        return new ResponseEntity<>(ErrorDto.builder()
            .errorId("OBJECT_NOT_FOUND")
            .message(exception.getMessage())
            .exception(exception.getClass().getName())
            .dateTime(LocalDateTime.now())
            .build(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception, @Nullable HttpHeaders headers,
        @Nullable HttpStatus status, @Nullable WebRequest request) {
        var errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", ", "[", "]"));
        return new ResponseEntity<>(ErrorDto.builder()
            .errorId("METHOD_ARGUMENT_NOT_VALID")
            .message(errors)
            .exception(exception.getClass().getName())
            .dateTime(LocalDateTime.now())
            .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
        ConstraintViolationException exception) {
        var errors = exception.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", ", "[", "]"));
        return new ResponseEntity<>(ErrorDto.builder()
            .errorId("METHOD_ARGUMENT_NOT_VALID")
            .message(errors)
            .exception(exception.getClass().getName())
            .dateTime(LocalDateTime.now())
            .build(), HttpStatus.BAD_REQUEST);
    }

}

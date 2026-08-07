package com.sk.skala.shopapi.exception;

import com.sk.skala.shopapi.common.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<Response> handleResponseException(ResponseException e) {
        Error error = e.getError();
        Response response = Response.builder()
                .resultCode(error.getCode())
                .resultMessage(e.getMessage())
                .body(null)
                .build();
        return ResponseEntity.status(error.getStatus()).body(response);
    }

    @ExceptionHandler(ParameterException.class)
    public ResponseEntity<Response> handleParameterException(ParameterException e) {
        Response response = Response.builder()
                .resultCode("INVALID_PARAMETER")
                .resultMessage(e.getMessage())
                .body(e.getParameters())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleException(Exception e) {
        Response response = Response.builder()
                .resultCode("INTERNAL_SERVER_ERROR")
                .resultMessage(e.getMessage())
                .body(null)
                .build();
        return ResponseEntity.internalServerError().body(response);
    }
}

package com.sk.skala.shopapi.exception;

import org.springframework.http.HttpStatus;

public enum Error {
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "DATA_NOT_FOUND", "Data not found"),
    DATA_DUPLICATED(HttpStatus.CONFLICT, "DATA_DUPLICATED", "Data duplicated"),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "NOT_AUTHENTICATED", "Authentication required"),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", "Insufficient funds"),
    INSUFFICIENT_QUANTITY(HttpStatus.BAD_REQUEST, "INSUFFICIENT_QUANTITY", "Insufficient quantity");

    private final HttpStatus status;
    private final String code;
    private final String message;

    Error(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

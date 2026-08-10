package com.sk.skala.shopapi.exception;

import org.springframework.http.HttpStatus;

public enum Error {

    DATA_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "DATA_NOT_FOUND",
            "데이터를 찾을 수 없습니다."
    ),

    DATA_DUPLICATED(
            HttpStatus.CONFLICT,
            "DATA_DUPLICATED",
            "이미 존재하는 데이터입니다."
    ),

    NOT_AUTHENTICATED(
            HttpStatus.UNAUTHORIZED,
            "NOT_AUTHENTICATED",
            "인증이 필요합니다."
    ),

    INSUFFICIENT_FUNDS(
            HttpStatus.BAD_REQUEST,
            "INSUFFICIENT_FUNDS",
            "보유 포인트가 부족합니다."
    ),

    INSUFFICIENT_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "INSUFFICIENT_QUANTITY",
            "취소 가능한 주문 수량이 부족합니다."
    ),

    OUT_OF_STOCK(
            HttpStatus.BAD_REQUEST,
            "OUT_OF_STOCK",
            "상품 재고가 부족합니다."
    );

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
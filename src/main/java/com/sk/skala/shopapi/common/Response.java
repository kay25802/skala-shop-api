package com.sk.skala.shopapi.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private String resultCode;
    private String resultMessage;
    private Object body;

    public static Response success(Object body) {
        return Response.builder()
                .resultCode("SUCCESS")
                .resultMessage("OK")
                .body(body)
                .build();
    }

    public static Response success(String message, Object body) {
        return Response.builder()
                .resultCode("SUCCESS")
                .resultMessage(message)
                .body(body)
                .build();
    }
}

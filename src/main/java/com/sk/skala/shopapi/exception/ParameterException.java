package com.sk.skala.shopapi.exception;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public class ParameterException extends RuntimeException {
    private final List<String> parameters;

    public ParameterException(String... parameters) {
        super("Invalid parameter: " + String.join(", ", parameters));
        this.parameters = Arrays.asList(parameters);
    }
}

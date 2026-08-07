package com.sk.skala.shopapi.data.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerSession {
    private String customerId;
    private String customerPassword;
}

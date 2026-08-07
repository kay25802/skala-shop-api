package com.sk.skala.shopapi.data.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {
    private Long productId;
    private Integer quantity;
}

package com.sk.skala.shopapi.controller;

import com.sk.skala.shopapi.common.Response;
import com.sk.skala.shopapi.data.table.Product;
import com.sk.skala.shopapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(
        name = "상품 API",
        description = "상품 조회, 등록, 수정, 삭제 기능을 제공하는 API"
)
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "전체 상품 목록 조회",
            description = "등록된 상품 목록을 페이지 단위로 조회합니다. offset과 count를 이용해 조회 범위를 지정할 수 있습니다."
    )
    @GetMapping
    public Response getAllProducts(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer count) {

        return productService.getAllProducts(offset, count);
    }

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID를 이용하여 특정 상품의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public Response getProductById(@PathVariable Long id) {

        return productService.getProductById(id);
    }

    @Operation(
            summary = "상품 등록",
            description = "상품명과 상품 가격을 입력하여 새로운 상품을 등록합니다."
    )
    @PostMapping
    public Response createProduct(@RequestBody Product product) {

        return productService.createProduct(product);
    }

    @Operation(
            summary = "상품 정보 수정",
            description = "상품 ID를 기준으로 상품명과 상품 가격을 수정합니다."
    )
    @PutMapping
    public Response updateProduct(@RequestBody Product product) {

        return productService.updateProduct(product);
    }

    @Operation(
            summary = "상품 삭제",
            description = "Request Body로 전달받은 상품 ID를 기준으로 상품을 삭제합니다."
    )
    @DeleteMapping
    public Response deleteProduct(@RequestBody Product product) {

        return productService.deleteProduct(product);
    }
}
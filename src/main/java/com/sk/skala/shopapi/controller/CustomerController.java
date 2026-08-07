package com.sk.skala.shopapi.controller;

import com.sk.skala.shopapi.common.Response;
import com.sk.skala.shopapi.data.dto.CustomerSession;
import com.sk.skala.shopapi.data.dto.OrderRequest;
import com.sk.skala.shopapi.data.table.Customer;
import com.sk.skala.shopapi.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(
        name = "고객 API",
        description = "고객 회원가입, 로그인, 조회, 수정, 삭제 및 상품 주문·취소 기능을 제공하는 API"
)
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
            summary = "전체 고객 목록 조회",
            description = "등록된 고객 목록을 페이지 단위로 조회합니다. offset과 count를 이용해 조회 범위를 지정할 수 있습니다."
    )
    @GetMapping
    public Response getAllCustomers(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "count", defaultValue = "10") int count) {

        return customerService.getAllCustomers(offset, count);
    }

    @Operation(
            summary = "고객 상세 조회",
            description = "고객 ID를 이용하여 고객 정보와 해당 고객이 주문한 상품 목록을 조회합니다."
    )
    @GetMapping("/{customerId}")
    public Response getCustomerById(@PathVariable String customerId) {

        return customerService.getCustomerById(customerId);
    }

    @Operation(
            summary = "고객 주문 상품 목록 조회",
            description = "고객 ID를 이용하여 해당 고객이 주문한 상품 목록과 보유 포인트를 조회합니다."
    )
    @GetMapping("/{customerId}/products")
    public Response getCustomerProducts(@PathVariable String customerId) {

        return customerService.getCustomerById(customerId);
    }

    @Operation(
            summary = "고객 회원가입",
            description = "고객 ID와 비밀번호를 입력하여 새로운 고객을 등록하고 초기 포인트를 지급합니다."
    )
    @PostMapping
    public Response createCustomer(@RequestBody Customer customer) {

        return customerService.createCustomer(customer);
    }

    @Operation(
            summary = "고객 로그인",
            description = "고객 ID와 비밀번호를 검증하고 로그인 성공 시 JWT 토큰을 발급합니다."
    )
    @PostMapping("/login")
    public Response loginCustomer(@RequestBody CustomerSession customerSession) {

        return customerService.loginCustomer(customerSession);
    }

    @Operation(
            summary = "고객 정보 수정",
            description = "고객 ID를 기준으로 고객 정보를 수정합니다. 주로 고객의 보유 포인트를 변경합니다."
    )
    @PutMapping
    public Response updateCustomer(@RequestBody Customer customer) {

        return customerService.updateCustomer(customer);
    }

    @Operation(
            summary = "고객 삭제",
            description = "고객 ID를 기준으로 해당 고객 정보를 삭제합니다."
    )
    @DeleteMapping("/{customerId}")
    public Response deleteCustomer(@PathVariable String customerId) {

        return customerService.deleteCustomerById(customerId);
    }

    @Operation(
            summary = "상품 주문",
            description = "로그인한 고객이 상품 ID와 수량을 입력하여 상품을 주문합니다. 주문 금액만큼 보유 포인트가 차감되며, 같은 상품을 다시 주문하면 기존 수량에 누적됩니다."
    )
    @PostMapping("/order")
    public Response placeOrder(@RequestBody OrderRequest order) {

        return customerService.placeOrder(order);
    }

    @Operation(
            summary = "상품 주문 취소",
            description = "로그인한 고객이 주문한 상품의 일부 또는 전체 수량을 취소합니다. 취소 수량만큼 주문 수량이 감소하고 해당 금액만큼 포인트가 환급되며, 수량이 0이 되면 주문 상품 정보가 삭제됩니다."
    )
    @PostMapping("/cancel")
    public Response cancelOrder(@RequestBody OrderRequest order) {

        return customerService.cancelOrder(order);
    }
}
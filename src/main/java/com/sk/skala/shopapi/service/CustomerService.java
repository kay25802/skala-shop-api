package com.sk.skala.shopapi.service;

import com.sk.skala.shopapi.common.PagedList;
import com.sk.skala.shopapi.common.Response;
import com.sk.skala.shopapi.common.SessionHandler;
import com.sk.skala.shopapi.data.dto.CustomerSession;
import com.sk.skala.shopapi.data.dto.OrderItemDto;
import com.sk.skala.shopapi.data.dto.OrderListDto;
import com.sk.skala.shopapi.data.dto.OrderRequest;
import com.sk.skala.shopapi.data.table.Customer;
import com.sk.skala.shopapi.data.table.OrderItem;
import com.sk.skala.shopapi.data.table.Product;
import com.sk.skala.shopapi.exception.Error;
import com.sk.skala.shopapi.exception.ParameterException;
import com.sk.skala.shopapi.exception.ResponseException;
import com.sk.skala.shopapi.repository.CustomerRepository;
import com.sk.skala.shopapi.repository.OrderItemRepository;
import com.sk.skala.shopapi.repository.ProductRepository;
import com.sk.skala.shopapi.tools.StringUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final double INITIAL_POINT = 1_000_000D;

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final SessionHandler sessionHandler;

    public Response getAllCustomers(int offset, int count) {

        if (offset < 0 || count <= 0) {
            throw new ParameterException("offset", "count");
        }

        Pageable pageable =
                PageRequest.of(
                        offset,
                        count,
                        Sort.by("customerId").ascending()
                );

        Page<Customer> page = customerRepository.findAll(pageable);

        page.getContent()
                .forEach(customer -> customer.setCustomerPassword(null));

        PagedList<Customer> pagedList =
                PagedList.<Customer>builder()
                        .items(page.getContent())
                        .page(page.getNumber())
                        .count(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return Response.success(pagedList);
    }

    @Transactional(readOnly = true)
    public Response getCustomerById(String customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResponseException(
                                Error.DATA_NOT_FOUND,
                                "고객 정보를 찾을 수 없습니다."
                        )
                );

        List<OrderItemDto> products =
                orderItemRepository.findByCustomer_CustomerId(customerId)
                        .stream()
                        .map(item ->
                                OrderItemDto.builder()
                                        .productId(item.getProduct().getId())
                                        .productName(
                                                item.getProduct()
                                                        .getProductName()
                                        )
                                        .productPrice(
                                                item.getProduct()
                                                        .getProductPrice()
                                        )
                                        .quantity(item.getQuantity())
                                        .build()
                        )
                        .toList();

        OrderListDto result =
                OrderListDto.builder()
                        .customerId(customer.getCustomerId())
                        .customerPoint(customer.getCustomerPoint())
                        .products(products)
                        .build();

        return Response.success(result);
    }

    public Response createCustomer(Customer customerSession) {

        if (customerSession == null
                || StringUtil.isAnyEmpty(
                        customerSession.getCustomerId(),
                        customerSession.getCustomerPassword()
                )) {

            throw new ParameterException(
                    "customerId",
                    "customerPassword"
            );
        }

        if (customerRepository.existsById(
                customerSession.getCustomerId())) {

            throw new ResponseException(
                    Error.DATA_DUPLICATED,
                    "이미 존재하는 고객 ID입니다."
            );
        }

        Customer customer =
                new Customer(
                        customerSession.getCustomerId(),
                        INITIAL_POINT
                );

        customer.setCustomerPassword(
                customerSession.getCustomerPassword()
        );

        Customer saved =
                customerRepository.save(customer);

        saved.setCustomerPassword(null);

        return Response.success(
                "회원가입이 완료되었습니다.",
                saved
        );
    }

    public Response loginCustomer(
            CustomerSession customerSession) {

        if (customerSession == null
                || StringUtil.isAnyEmpty(
                        customerSession.getCustomerId(),
                        customerSession.getCustomerPassword()
                )) {

            throw new ParameterException(
                    "customerId",
                    "customerPassword"
            );
        }

        Customer customer =
                customerRepository
                        .findById(
                                customerSession.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "고객 정보를 찾을 수 없습니다."
                                )
                        );

        if (!customer.getCustomerPassword()
                .equals(
                        customerSession.getCustomerPassword()
                )) {

            throw new ResponseException(
                    Error.NOT_AUTHENTICATED,
                    "비밀번호가 일치하지 않습니다."
            );
        }

        sessionHandler.storeAccessToken(
                customer.getCustomerId()
        );

        customer.setCustomerPassword(null);

        return Response.success(
                "로그인에 성공했습니다.",
                customer
        );
    }

    public Response updateCustomer(Customer customer) {

        if (customer == null
                || StringUtil.isEmpty(
                        customer.getCustomerId()
                )
                || customer.getCustomerPoint() == null
                || customer.getCustomerPoint() < 0) {

            throw new ParameterException(
                    "customerId",
                    "customerPoint"
            );
        }

        Customer saved =
                customerRepository
                        .findById(
                                customer.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "고객 정보를 찾을 수 없습니다."
                                )
                        );

        saved.setCustomerPoint(
                customer.getCustomerPoint()
        );

        Customer updated =
                customerRepository.save(saved);

        updated.setCustomerPassword(null);

        return Response.success(
                "고객 정보가 수정되었습니다.",
                updated
        );
    }

    @Transactional
    public Response deleteCustomer(Customer customer) {

        if (customer == null
                || StringUtil.isEmpty(
                        customer.getCustomerId()
                )) {

            throw new ParameterException(
                    "customerId"
            );
        }

        return deleteCustomerById(
                customer.getCustomerId()
        );
    }

    @Transactional
    public Response deleteCustomerById(
            String customerId) {

        Customer saved =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "고객 정보를 찾을 수 없습니다."
                                )
                        );

        orderItemRepository.deleteByCustomer(saved);
        customerRepository.delete(saved);

        saved.setCustomerPassword(null);

        return Response.success(
                "고객 정보가 삭제되었습니다.",
                saved
        );
    }

    /*
     * 상품 주문
     *
     * 차별화 기능:
     * 1. 주문 전 재고 확인
     * 2. 주문 수량만큼 재고 차감
     * 3. 재고 부족 시 OUT_OF_STOCK 예외
     */
    @Transactional
    public Response placeOrder(OrderRequest order) {

        validateOrder(order);

        String customerId =
                sessionHandler.getCustomerId();

        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "고객 정보를 찾을 수 없습니다."
                                )
                        );

        Product product =
                productRepository
                        .findById(order.getProductId())
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "상품 정보를 찾을 수 없습니다."
                                )
                        );

        /*
         * ========================================
         * 차별화 기능 1
         * 재고 확인
         * ========================================
         */
        if (product.getStock() == null
                || product.getStock()
                < order.getQuantity()) {

            throw new ResponseException(
                    Error.OUT_OF_STOCK
            );
        }

        /*
         * 주문 금액 계산
         */
        double totalPrice =
                product.getProductPrice()
                        * order.getQuantity();

        /*
         * 포인트 확인
         */
        if (customer.getCustomerPoint()
                < totalPrice) {

            throw new ResponseException(
                    Error.INSUFFICIENT_FUNDS
            );
        }

        /*
         * 포인트 차감
         */
        customer.setCustomerPoint(
                customer.getCustomerPoint()
                        - totalPrice
        );

        /*
         * ========================================
         * 차별화 기능 2
         * 상품 재고 차감
         * ========================================
         */
        product.setStock(
                product.getStock()
                        - order.getQuantity()
        );

        /*
         * 기존 주문 상품이 있는지 확인
         *
         * 이미 주문했던 상품이면 수량 누적
         * 처음 주문하는 상품이면 새 OrderItem 생성
         */
        OrderItem item =
                orderItemRepository
                        .findByCustomerAndProduct(
                                customer,
                                product
                        )
                        .orElseGet(() ->
                                new OrderItem(
                                        customer,
                                        product,
                                        0
                                )
                        );

        item.setQuantity(
                item.getQuantity()
                        + order.getQuantity()
        );

        /*
         * DB 저장
         */
        customerRepository.save(customer);

        productRepository.save(product);

        orderItemRepository.save(item);

        return Response.success(
                "주문이 완료되었습니다.",
                customer.getCustomerPoint()
        );
    }

    /*
     * 주문 취소
     *
     * 차별화 기능:
     * 주문 취소 수량만큼 상품 재고 복구
     */
    @Transactional
    public Response cancelOrder(
            OrderRequest order) {

        validateOrder(order);

        String customerId =
                sessionHandler.getCustomerId();

        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "고객 정보를 찾을 수 없습니다."
                                )
                        );

        Product product =
                productRepository
                        .findById(order.getProductId())
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.DATA_NOT_FOUND,
                                        "상품 정보를 찾을 수 없습니다."
                                )
                        );

        OrderItem item =
                orderItemRepository
                        .findByCustomerAndProduct(
                                customer,
                                product
                        )
                        .orElseThrow(() ->
                                new ResponseException(
                                        Error.INSUFFICIENT_QUANTITY
                                )
                        );

        /*
         * 취소하려는 수량이
         * 실제 주문 수량보다 많으면 취소 불가
         */
        if (item.getQuantity()
                < order.getQuantity()) {

            throw new ResponseException(
                    Error.INSUFFICIENT_QUANTITY
            );
        }

        /*
         * 남은 주문 수량 계산
         */
        int remain =
                item.getQuantity()
                        - order.getQuantity();

        /*
         * 남은 주문 수량이 0이면
         * OrderItem 자체 삭제
         */
        if (remain == 0) {

            orderItemRepository.delete(item);

        } else {

            item.setQuantity(remain);
            orderItemRepository.save(item);
        }

        /*
         * 환불 금액 계산
         */
        double refund =
                product.getProductPrice()
                        * order.getQuantity();

        /*
         * 포인트 환급
         */
        customer.setCustomerPoint(
                customer.getCustomerPoint()
                        + refund
        );

        /*
         * ========================================
         * 차별화 기능 3
         * 주문 취소한 수량만큼 재고 복구
         * ========================================
         */
        product.setStock(
                product.getStock()
                        + order.getQuantity()
        );

        /*
         * DB 저장
         */
        customerRepository.save(customer);

        productRepository.save(product);

        return Response.success(
                "주문 취소가 완료되었습니다.",
                customer.getCustomerPoint()
        );
    }

    /*
     * 주문 요청 검증
     */
    private void validateOrder(
            OrderRequest order) {

        if (order == null
                || order.getProductId() == null
                || order.getQuantity() == null
                || order.getQuantity() <= 0) {

            throw new ParameterException(
                    "productId",
                    "quantity"
            );
        }
    }
}
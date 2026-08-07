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

        Pageable pageable = PageRequest.of(offset, count, Sort.by("customerId").ascending());
        Page<Customer> page = customerRepository.findAll(pageable);
        page.getContent().forEach(customer -> customer.setCustomerPassword(null));

        PagedList<Customer> pagedList = PagedList.<Customer>builder()
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
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));

        List<OrderItemDto> products = orderItemRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getProductName())
                        .productPrice(item.getProduct().getProductPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        OrderListDto result = OrderListDto.builder()
                .customerId(customer.getCustomerId())
                .customerPoint(customer.getCustomerPoint())
                .products(products)
                .build();

        return Response.success(result);
    }

    public Response createCustomer(Customer customerSession) {
        if (customerSession == null || StringUtil.isAnyEmpty(
                customerSession.getCustomerId(), customerSession.getCustomerPassword())) {
            throw new ParameterException("customerId", "customerPassword");
        }

        if (customerRepository.existsById(customerSession.getCustomerId())) {
            throw new ResponseException(Error.DATA_DUPLICATED, "Customer ID already exists");
        }

        Customer customer = new Customer(customerSession.getCustomerId(), INITIAL_POINT);
        customer.setCustomerPassword(customerSession.getCustomerPassword());
        Customer saved = customerRepository.save(customer);
        saved.setCustomerPassword(null);
        return Response.success("Customer created", saved);
    }

    public Response loginCustomer(CustomerSession customerSession) {
        if (customerSession == null || StringUtil.isAnyEmpty(
                customerSession.getCustomerId(), customerSession.getCustomerPassword())) {
            throw new ParameterException("customerId", "customerPassword");
        }

        Customer customer = customerRepository.findById(customerSession.getCustomerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));

        if (!customer.getCustomerPassword().equals(customerSession.getCustomerPassword())) {
            throw new ResponseException(Error.NOT_AUTHENTICATED, "Invalid password");
        }

        sessionHandler.storeAccessToken(customer.getCustomerId());
        customer.setCustomerPassword(null);
        return Response.success("Login success", customer);
    }

    public Response updateCustomer(Customer customer) {
        if (customer == null || StringUtil.isEmpty(customer.getCustomerId())
                || customer.getCustomerPoint() == null || customer.getCustomerPoint() < 0) {
            throw new ParameterException("customerId", "customerPoint");
        }

        Customer saved = customerRepository.findById(customer.getCustomerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));
        saved.setCustomerPoint(customer.getCustomerPoint());
        Customer updated = customerRepository.save(saved);
        updated.setCustomerPassword(null);
        return Response.success("Customer updated", updated);
    }

    @Transactional
    public Response deleteCustomer(Customer customer) {
        if (customer == null || StringUtil.isEmpty(customer.getCustomerId())) {
            throw new ParameterException("customerId");
        }
        return deleteCustomerById(customer.getCustomerId());
    }

    @Transactional
    public Response deleteCustomerById(String customerId) {
        Customer saved = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));
        orderItemRepository.deleteByCustomer(saved);
        customerRepository.delete(saved);
        saved.setCustomerPassword(null);
        return Response.success("Customer deleted", saved);
    }

    @Transactional
    public Response placeOrder(OrderRequest order) {
        validateOrder(order);
        String customerId = sessionHandler.getCustomerId();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Product not found"));

        double totalPrice = product.getProductPrice() * order.getQuantity();
        if (customer.getCustomerPoint() < totalPrice) {
            throw new ResponseException(Error.INSUFFICIENT_FUNDS);
        }

        customer.setCustomerPoint(customer.getCustomerPoint() - totalPrice);

        OrderItem item = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseGet(() -> new OrderItem(customer, product, 0));
        item.setQuantity(item.getQuantity() + order.getQuantity());

        customerRepository.save(customer);
        orderItemRepository.save(item);

        return Response.success("Order completed", customer.getCustomerPoint());
    }

    @Transactional
    public Response cancelOrder(OrderRequest order) {
        validateOrder(order);
        String customerId = sessionHandler.getCustomerId();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Customer not found"));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Product not found"));

        OrderItem item = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new ResponseException(Error.INSUFFICIENT_QUANTITY));

        if (item.getQuantity() < order.getQuantity()) {
            throw new ResponseException(Error.INSUFFICIENT_QUANTITY);
        }

        int remain = item.getQuantity() - order.getQuantity();
        if (remain == 0) {
            orderItemRepository.delete(item);
        } else {
            item.setQuantity(remain);
            orderItemRepository.save(item);
        }

        double refund = product.getProductPrice() * order.getQuantity();
        customer.setCustomerPoint(customer.getCustomerPoint() + refund);
        customerRepository.save(customer);

        return Response.success("Order canceled", customer.getCustomerPoint());
    }

    private void validateOrder(OrderRequest order) {
        if (order == null || order.getProductId() == null
                || order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new ParameterException("productId", "quantity");
        }
    }
}

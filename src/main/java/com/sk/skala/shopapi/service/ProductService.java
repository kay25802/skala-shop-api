package com.sk.skala.shopapi.service;

import com.sk.skala.shopapi.common.PagedList;
import com.sk.skala.shopapi.common.Response;
import com.sk.skala.shopapi.data.table.Product;
import com.sk.skala.shopapi.exception.Error;
import com.sk.skala.shopapi.exception.ParameterException;
import com.sk.skala.shopapi.exception.ResponseException;
import com.sk.skala.shopapi.repository.ProductRepository;
import com.sk.skala.shopapi.tools.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Response getAllProducts(int offset, int count) {
        if (offset < 0 || count <= 0) {
            throw new ParameterException("offset", "count");
        }

        Pageable pageable = PageRequest.of(offset, count, Sort.by("id").ascending());
        Page<Product> page = productRepository.findAll(pageable);

        PagedList<Product> pagedList = PagedList.<Product>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .count(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return Response.success(pagedList);
    }

    public Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Product not found"));
        return Response.success(product);
    }

    public Response createProduct(Product product) {
        validateProduct(product);

        if (productRepository.findByProductName(product.getProductName()).isPresent()) {
            throw new ResponseException(Error.DATA_DUPLICATED, "Product name already exists");
        }

        product.setId(null);
        return Response.success("Product created", productRepository.save(product));
    }

    public Response updateProduct(Product product) {
        validateProduct(product);
        if (product.getId() == null) {
            throw new ParameterException("id");
        }

        Product saved = productRepository.findById(product.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Product not found"));

        productRepository.findByProductName(product.getProductName())
                .filter(other -> !other.getId().equals(product.getId()))
                .ifPresent(other -> {
                    throw new ResponseException(Error.DATA_DUPLICATED, "Product name already exists");
                });

        saved.setProductName(product.getProductName());
        saved.setProductPrice(product.getProductPrice());
        return Response.success("Product updated", productRepository.save(saved));
    }

    public Response deleteProduct(Product product) {
        if (product == null || product.getId() == null) {
            throw new ParameterException("id");
        }
        return deleteProductById(product.getId());
    }

    public Response deleteProductById(Long id) {
        Product saved = productRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Product not found"));
        productRepository.delete(saved);
        return Response.success("Product deleted", saved);
    }

    private void validateProduct(Product product) {
        if (product == null || StringUtil.isEmpty(product.getProductName())
                || product.getProductPrice() == null || product.getProductPrice() <= 0) {
            throw new ParameterException("productName", "productPrice");
        }
    }
}

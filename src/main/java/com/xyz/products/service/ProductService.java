package com.xyz.products.service;

import com.xyz.products.common.Product;
import com.xyz.products.dto.ProductRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Product create(ProductRequest req);

    Page<Product> list(String name, String description, Pageable pageable);

    Product get(Long id);

    Product update(Long id, ProductRequest req);

    void delete(Long id);
}

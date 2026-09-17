package com.xyz.products.config;

import com.xyz.products.repository.ProductRepository;
import com.xyz.products.service.ProductService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public ProductService productService(ProductRepository repository) {
        return new ProductService(repository);
    }
}

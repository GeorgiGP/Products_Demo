package com.xyz.products.service;

import com.xyz.products.common.Product;
import com.xyz.products.dto.ProductRequest;

import com.xyz.products.exceptions.ProductNotFoundException;
import com.xyz.products.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;

@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product create(ProductRequest req) {
        Product product = new Product(req.name(), req.price(), req.category(), req.quantity(), req.description());
        Product saved = repository.save(product);
        log.info("Created product id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> list(String name, String description, Pageable pageable) {
        Page<Product> page = repository.search(blankToNull(name), blankToNull(description), pageable);
        log.debug("Listed page {} ({} of {} products, name={}, description={})",
                page.getNumber(), page.getNumberOfElements(), page.getTotalElements(), name, description);
        return page;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public Product get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(format("Product not found: id=%s", id)));
    }

    @Override
    public Product update(Long id, ProductRequest req) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(format("Update failed, product not found: id=%s", id)));

        product.setName(req.name());
        product.setPrice(req.price());
        product.setCategory(req.category());
        product.setQuantity(req.quantity());
        product.setDescription(req.description());

        Product saved = repository.save(product);
        log.info("Updated product id={}", saved.getId());
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(format("Delete failed, product not found: id=%s", id));
        }
        repository.deleteById(id);
        log.info("Deleted product id={}", id);
    }
}

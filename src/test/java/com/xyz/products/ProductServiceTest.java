package com.xyz.products;

import com.xyz.products.common.Product;
import com.xyz.products.dto.ProductRequest;

import com.xyz.products.exceptions.ProductNotFoundException;
import com.xyz.products.repository.ProductRepository;
import com.xyz.products.service.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    private static ProductRequest sampleRequest() {
        return new ProductRequest("Widget", new BigDecimal("9.99"), "tools", 5, "a handy widget");
    }

    @Test
    void givenValidRequest_whenCreate_thenProductIsPersisted() {
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sampleRequest());

        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getPrice()).isEqualByComparingTo("9.99");
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getDescription()).isEqualTo("a handy widget");
        verify(repository).save(any(Product.class));
    }

    @Test
    void givenMissingId_whenGet_thenThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void givenExistingProduct_whenUpdate_thenNewValuesAreApplied() {
        Product existing = new Product("Old", new BigDecimal("1.00"), "misc", 1, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = service.update(1L,
                new ProductRequest("New", new BigDecimal("2.50"), "tools", 3, "updated desc"));

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getPrice()).isEqualByComparingTo("2.50");
        assertThat(updated.getCategory()).isEqualTo("tools");
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getDescription()).isEqualTo("updated desc");
    }

    @Test
    void givenMissingId_whenDelete_thenThrowsAndSkipsDeletion() {
        when(repository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(ProductNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}

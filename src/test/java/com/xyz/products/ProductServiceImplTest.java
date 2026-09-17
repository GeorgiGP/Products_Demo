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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    private static ProductRequest sampleRequest() {
        return new ProductRequest("Widget", new BigDecimal("9.99"), "tools", 5, "a handy widget");
    }

    @Test
    void givenValidRequest_whenCreate_thenPersistsMappedEntityAndReturnsSaved() {
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(sampleRequest());

        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getPrice()).isEqualByComparingTo("9.99");
        assertThat(result.getCategory()).isEqualTo("tools");
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getDescription()).isEqualTo("a handy widget");
        verify(repository).save(any(Product.class));
    }

    @Test
    void givenNullDescription_whenCreate_thenDescriptionIsNull() {
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(
                new ProductRequest("NoDesc", new BigDecimal("1.00"), "misc", 0, null));

        assertThat(result.getDescription()).isNull();
        assertThat(result.getQuantity()).isZero();
    }

    @Test
    void givenBlankFilters_whenList_thenPassedAsNullToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.search(any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        service.list("   ", "", pageable);

        verify(repository).search(eq(null), eq(null), eq(pageable));
    }

    @Test
    void givenNullFilters_whenList_thenPassedAsNullToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.search(any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(null, null, pageable);

        verify(repository).search(eq(null), eq(null), eq(pageable));
    }

    @Test
    void givenPaddedFilters_whenList_thenTrimmedAndPassedThrough() {
        Pageable pageable = PageRequest.of(1, 5);
        Product p = new Product("Hammer", new BigDecimal("3.00"), "tools", 2, "steel");
        Page<Product> expected = new PageImpl<>(List.of(p));
        when(repository.search(eq("hammer"), eq("steel"), eq(pageable))).thenReturn(expected);

        Page<Product> result = service.list("  hammer  ", "  steel  ", pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).search(eq("hammer"), eq("steel"), eq(pageable));
    }

    @Test
    void givenExistingId_whenGet_thenReturnsProduct() {
        Product existing = new Product("Bolt", new BigDecimal("0.50"), "hardware", 100, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThat(service.get(1L)).isSameAs(existing);
    }

    @Test
    void givenMissingId_whenGet_thenThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void givenExistingProduct_whenUpdate_thenNewValuesAreAppliedAndSaved() {
        Product existing = new Product("Old", new BigDecimal("1.00"), "misc", 1, "old desc");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = service.update(1L,
                new ProductRequest("New", new BigDecimal("2.50"), "tools", 3, "updated desc"));

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getPrice()).isEqualByComparingTo("2.50");
        assertThat(updated.getCategory()).isEqualTo("tools");
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getDescription()).isEqualTo("updated desc");
        verify(repository).save(existing);
    }

    @Test
    void givenNullDescription_whenUpdate_thenDescriptionCleared() {
        Product existing = new Product("Old", new BigDecimal("1.00"), "misc", 1, "old desc");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = service.update(1L,
                new ProductRequest("New", new BigDecimal("2.50"), "tools", 3, null));

        assertThat(updated.getDescription()).isNull();
    }

    @Test
    void givenMissingId_whenUpdate_thenThrowsNotFoundAndNeverSaves() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, sampleRequest()))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("9");
        verify(repository, never()).save(any());
    }

    @Test
    void givenExistingId_whenDelete_thenDeletesById() {
        when(repository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    @Test
    void givenMissingId_whenDelete_thenThrowsAndSkipsDeletion() {
        when(repository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("7");
        verify(repository, never()).deleteById(any());
    }
}

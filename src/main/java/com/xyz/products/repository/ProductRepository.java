package com.xyz.products.repository;

import com.xyz.products.common.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Optional, independent substring filters on name and/or description
     * (case-insensitive). A {@code null} parameter disables that filter; when
     * both are given they combine with AND. A {@code null} description column
     * never matches the description filter.
     */
    @Query("""
            select p from Product p
            where (cast(:name as string) is null or lower(p.name) like lower(concat('%', cast(:name as string), '%')))
              and (cast(:description as string) is null or lower(p.description) like lower(concat('%', cast(:description as string), '%')))
            """)
    Page<Product> search(@Param("name") String name,
                         @Param("description") String description,
                         Pageable pageable);
}

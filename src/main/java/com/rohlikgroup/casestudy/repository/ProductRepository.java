package com.rohlikgroup.casestudy.repository;

import com.rohlikgroup.casestudy.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("UPDATE Product p SET p.stockAmount = p.stockAmount + :quantity WHERE p.id = :productId")
    void updateStockAmount(@Param("productId") Long productId, @Param("quantity") Integer quantity);

}

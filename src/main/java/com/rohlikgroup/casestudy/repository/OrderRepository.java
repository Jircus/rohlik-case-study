package com.rohlikgroup.casestudy.repository;

import com.rohlikgroup.casestudy.entity.Order;
import com.rohlikgroup.casestudy.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderItemsProductIdAndStatusNotIn(Long productId, Set<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :timestamp")
    List<Order> findAllByStatusAndCreatedAtBefore(@Param("status") OrderStatus status, @Param("timestamp") LocalDateTime timestamp);

    @Modifying
    @Query("UPDATE Order o SET o = :status WHERE o.id = :orderId")
    void updateStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

}

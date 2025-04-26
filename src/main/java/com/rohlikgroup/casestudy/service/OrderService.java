package com.rohlikgroup.casestudy.service;

import com.rohlikgroup.casestudy.dto.OrderDto;
import com.rohlikgroup.casestudy.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public interface OrderService {

    /**
     * Creates a new order.
     *
     * @param order the order to create
     * @return the created order
     */
    OrderDto createOrder(@Valid @NotNull CreateOrderRequest order);

    /**
     * Cancels an order.
     *
     * @param orderId the ID of the order to cancel
     * @return the canceled order
     */
    OrderDto cancelOrder(@NotNull Long orderId);

    /**
     * Pays an order.
     *
     * @param orderId the ID of the order to pay
     * @return the paid order
     */
    OrderDto setOrderPaid(@NotNull Long orderId);

    List<OrderDto> getOrders();

    void releaseUnpaidOrders();

}

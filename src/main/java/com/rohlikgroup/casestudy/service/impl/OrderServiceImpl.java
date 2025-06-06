package com.rohlikgroup.casestudy.service.impl;

import com.rohlikgroup.casestudy.dto.CreateOrderRequest;
import com.rohlikgroup.casestudy.dto.OrderDto;
import com.rohlikgroup.casestudy.dto.OrderItemRequest;
import com.rohlikgroup.casestudy.entity.Order;
import com.rohlikgroup.casestudy.entity.OrderItem;
import com.rohlikgroup.casestudy.entity.OrderStatus;
import com.rohlikgroup.casestudy.exception.InsufficientStockException;
import com.rohlikgroup.casestudy.mapper.OrderMapper;
import com.rohlikgroup.casestudy.repository.OrderRepository;
import com.rohlikgroup.casestudy.repository.ProductRepository;
import com.rohlikgroup.casestudy.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest orderRequest) {
        boolean hasDuplicates = orderRequest.orderItems().stream()
            .map(OrderItemRequest::productId)
            .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
            .values().stream()
            .anyMatch(count -> count > 1);

        if (hasDuplicates) {
            throw new IllegalArgumentException("Duplicate product IDs are not allowed in the order");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        var orderItems = orderRequest.orderItems().stream().map(orderItemRequest -> {
            try {
                //stock amount is updated atomically in the database we don't need to lock the product and slow down the process
                //also there is a check in the database that prevents negative stock amounts
                productRepository.updateStockAmount(orderItemRequest.productId(), orderItemRequest.quantity() * -1);
            } catch (DataIntegrityViolationException e) {
                throw new InsufficientStockException("Insufficient stock for product with id: " + orderItemRequest.productId());
            }

            var product = productRepository.findById(orderItemRequest.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + orderItemRequest.productId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(orderItemRequest.quantity());
            orderItem.setOrder(order);

            return orderItem;
        }).toList();

        order.setOrderItems(orderItems);

        return orderMapper.map(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Cannot cancel order in status " + order.getStatus());
        }

        //first we cancel the order - if it changes in the meantime the optimistic locking exception will be thrown, and we won't update the stock amounts
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        for (OrderItem item : order.getOrderItems()) {
            productRepository.updateStockAmount(item.getProduct().getId(), item.getQuantity());
        }


        return orderMapper.map(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be paid for");
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        return orderMapper.map(order);
    }

    @Override
    public List<OrderDto> getOrders() {
        return orderRepository.findAll().stream().map(orderMapper::map).toList();
    }

    @Override
    @Transactional
    public void releaseUnpaidOrders() {
        List<Order> orders = orderRepository.findAllByStatusAndCreatedAtBefore(OrderStatus.PENDING, LocalDateTime.now().minusMinutes(30));

        for (Order order : orders) {
            try {
                cancelOrder(order.getId());
            } catch (OptimisticLockingFailureException e) {
                log.error("Failed to process order {}: {}", order.getId(), e.getMessage());
            }
        }
    }

}

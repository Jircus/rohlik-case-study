package com.rohlikgroup.casestudy.service.impl;

import com.rohlikgroup.casestudy.dto.CreateOrderRequest;
import com.rohlikgroup.casestudy.dto.OrderDto;
import com.rohlikgroup.casestudy.dto.OrderItemRequest;
import com.rohlikgroup.casestudy.entity.Order;
import com.rohlikgroup.casestudy.entity.OrderItem;
import com.rohlikgroup.casestudy.entity.OrderStatus;
import com.rohlikgroup.casestudy.entity.Product;
import com.rohlikgroup.casestudy.exception.InsufficientStockException;
import com.rohlikgroup.casestudy.mapper.OrderMapper;
import com.rohlikgroup.casestudy.repository.OrderRepository;
import com.rohlikgroup.casestudy.repository.ProductRepository;
import com.rohlikgroup.casestudy.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jiří Škoda, jiri.skoda@media-sol.com, MEDIA SOLUTIONS
 */
@ExtendWith(SpringExtension.class)
class OrderServiceImplTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public OrderService orderServiceTest(ApplicationContext ctx) {
            return ctx.getAutowireCapableBeanFactory().createBean(OrderServiceImpl.class);
        }

        @Bean
        public Validator validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        public MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }

    }

    @Autowired
    private OrderService orderService;
    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private OrderMapper orderMapper;

    @TestFactory
    List<DynamicNode> createOrder() {
        Product p = new Product();
        p.setId(1L);
        p.setStockAmount(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        return List.of(
                dynamicTest("Null DTO", () -> assertThrows(ConstraintViolationException.class, () -> orderService.createOrder(null))),
                dynamicTest("Invalid DTO", () -> assertThrows(ConstraintViolationException.class, () -> orderService.createOrder(new CreateOrderRequest(List.of(new OrderItemRequest(1L, 0)))))),
                dynamicTest("Duplicated order item", () -> assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(new CreateOrderRequest(List.of(
                        new OrderItemRequest(1L, 1),
                        new OrderItemRequest(1L, 1)
                ))))),
                dynamicTest("Insufficient stock", () -> assertThrows(InsufficientStockException.class, () -> orderService.createOrder(new CreateOrderRequest(List.of(new OrderItemRequest(1L, 10)))))),
                dynamicTest("OK", () -> {
                    when(orderMapper.map(Mockito.any(Order.class))).then(a -> new OrderDto(1L, a.<Order>getArgument(0).getStatus().toString(), List.of(), null));
                    when(orderRepository.save(Mockito.any(Order.class))).then(a -> a.getArgument(0));

                    OrderDto result = orderService.createOrder(new CreateOrderRequest(List.of(
                            new OrderItemRequest(1L, 4)
                    )));

                    assertEquals(new OrderDto(1L, "PENDING", List.of(), null), result);

                    verify(productRepository).updateStockAmount(1L, -4);
                })
        );
    }

    @Test
    void releaseUnpaidOrders() {
        Order p = new Order();
        p.setId(1L);
        p.setStatus(OrderStatus.PENDING);
        OrderItem orderItem = new OrderItem();
        Product product = new Product();
        product.setId(2L);
        orderItem.setProduct(product);
        orderItem.setQuantity(5);
        p.setOrderItems(List.of(orderItem));
        when(orderRepository.findAllByStatusAndCreatedAtBefore(Mockito.eq(OrderStatus.PENDING), Mockito.any(LocalDateTime.class))).thenReturn(List.of(p));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(p));

        orderService.releaseUnpaidOrders();

        Order p2 = new Order();
        p2.setId(1L);
        p2.setStatus(OrderStatus.CANCELED);
        p2.setOrderItems(List.of(orderItem));
        verify(orderRepository).save(p2);
        verify(productRepository).updateStockAmount(2L, 5);

    }

}
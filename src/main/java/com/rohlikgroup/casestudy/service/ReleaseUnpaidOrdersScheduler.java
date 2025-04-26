package com.rohlikgroup.casestudy.service;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReleaseUnpaidOrdersScheduler {

    private final OrderService orderService;

    @Scheduled(fixedRate = 5000)
    public void releaseUnpaidOrders() {
        orderService.releaseUnpaidOrders();
    }

}

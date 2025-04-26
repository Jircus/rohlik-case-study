package com.rohlikgroup.casestudy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(Long id,
                       @NotBlank
                       String status,
                       @Size(min = 1)
                       List<@Valid OrderItemDto> orderItems,
                       LocalDateTime paidAt) {

}

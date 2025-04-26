package com.rohlikgroup.casestudy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductDto(Long id,
                         @NotBlank
                         @Size(max = 100)
                         String name,
                         @NotNull
                         @Min(0)
                         Integer stockAmount,
                         @NotNull
                         @Positive
                         BigDecimal price) {

}

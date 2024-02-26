package com.khachidze.moneytransferservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AmountDto {
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
}

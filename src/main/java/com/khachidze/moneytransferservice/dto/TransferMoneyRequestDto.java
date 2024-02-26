package com.khachidze.moneytransferservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferMoneyRequestDto {
    @JsonProperty("sender_phone")
    private String senderPhone;
    @JsonProperty("receiver_phone")
    private String receiverPhone;
    private BigDecimal amount;
}

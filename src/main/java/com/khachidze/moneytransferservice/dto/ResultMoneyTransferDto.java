package com.khachidze.moneytransferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultMoneyTransferDto {
    private UserDto sender;
    private UserDto receiver;
}

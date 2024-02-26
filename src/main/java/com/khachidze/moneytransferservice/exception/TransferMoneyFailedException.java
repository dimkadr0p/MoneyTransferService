package com.khachidze.moneytransferservice.exception;

public class TransferMoneyFailedException extends RuntimeException {
    public TransferMoneyFailedException(String message) {
        super(message);
    }
}

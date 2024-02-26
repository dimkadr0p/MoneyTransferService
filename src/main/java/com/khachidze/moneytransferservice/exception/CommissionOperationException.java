package com.khachidze.moneytransferservice.exception;

public class CommissionOperationException extends RuntimeException {
    public CommissionOperationException(String message) {
        super(message);
    }
}

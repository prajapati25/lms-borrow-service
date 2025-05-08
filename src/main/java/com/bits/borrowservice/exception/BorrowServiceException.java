package com.bits.borrowservice.exception;

public class BorrowServiceException extends RuntimeException {
    public BorrowServiceException(String message) {
        super(message);
    }

    public BorrowServiceException(String message, Throwable cause) {
        super(message, cause);
    }
} 
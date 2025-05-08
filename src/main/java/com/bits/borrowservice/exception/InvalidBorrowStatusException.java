package com.bits.borrowservice.exception;
 
public class InvalidBorrowStatusException extends BorrowServiceException {
    public InvalidBorrowStatusException(String message) {
        super(message);
    }
} 
package com.bits.borrowservice.exception;
 
public class BookNotAvailableException extends BorrowServiceException {
    public BookNotAvailableException(String message) {
        super(message);
    }
} 
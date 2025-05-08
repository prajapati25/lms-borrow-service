package com.bits.borrowservice.exception;
 
public class BookAlreadyReturnedException extends BorrowServiceException {
    public BookAlreadyReturnedException(String message) {
        super(message);
    }
} 
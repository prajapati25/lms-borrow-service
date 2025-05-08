package com.bits.borrowservice.exception;
 
public class MaximumBorrowsExceededException extends BorrowServiceException {
    public MaximumBorrowsExceededException(String message) {
        super(message);
    }
} 
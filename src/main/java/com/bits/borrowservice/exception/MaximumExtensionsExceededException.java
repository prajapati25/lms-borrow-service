package com.bits.borrowservice.exception;
 
public class MaximumExtensionsExceededException extends BorrowServiceException {
    public MaximumExtensionsExceededException(String message) {
        super(message);
    }
} 
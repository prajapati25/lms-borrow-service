package com.bits.borrowservice.exception;
 
public class UserNotActiveException extends BorrowServiceException {
    public UserNotActiveException(String message) {
        super(message);
    }
} 
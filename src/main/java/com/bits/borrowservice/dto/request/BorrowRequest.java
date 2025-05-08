package com.bits.borrowservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
} 
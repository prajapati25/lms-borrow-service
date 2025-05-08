package com.bits.borrowservice.dto.response;

import com.bits.borrowservice.entity.Borrow;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private Borrow.BorrowStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BorrowResponse fromEntity(Borrow borrow) {
        BorrowResponse response = new BorrowResponse();
        response.setId(borrow.getId());
        response.setUserId(borrow.getUserId());
        response.setBookId(borrow.getBookId());
        response.setBorrowDate(borrow.getBorrowDate());
        response.setDueDate(borrow.getDueDate());
        response.setStatus(borrow.getStatus());
        response.setCreatedAt(borrow.getCreatedAt());
        response.setUpdatedAt(borrow.getUpdatedAt());
        return response;
    }
} 
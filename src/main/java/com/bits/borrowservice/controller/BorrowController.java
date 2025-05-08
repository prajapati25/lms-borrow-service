package com.bits.borrowservice.controller;

import com.bits.borrowservice.dto.request.BorrowRequest;
import com.bits.borrowservice.dto.response.BorrowResponse;
import com.bits.borrowservice.exception.BorrowNotFoundException;
import com.bits.borrowservice.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@Tag(name = "Borrow Service", description = "APIs for managing book borrows")
public class BorrowController {
    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);
    private final BorrowService borrowService;

    @GetMapping
    @Operation(summary = "List all borrow records")
    public ResponseEntity<Page<BorrowResponse>> getAllBorrows(Pageable pageable) {
        logger.debug("Getting all borrows with pageable: {}", pageable);
        Page<BorrowResponse> borrows = borrowService.getAllBorrows(pageable);
        logger.debug("Found {} borrows", borrows.getTotalElements());
        return ResponseEntity.ok(borrows);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get borrow record details")
    public ResponseEntity<BorrowResponse> getBorrow(@PathVariable Long id) {
        logger.debug("Getting borrow with ID: {}", id);
        BorrowResponse borrow = borrowService.getUserBorrows(id, Pageable.unpaged())
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Borrow not found with ID: {}", id);
                    return new BorrowNotFoundException("Borrow record not found");
                });
        logger.debug("Found borrow: {}", borrow);
        return ResponseEntity.ok(borrow);
    }

    @PostMapping
    @Operation(summary = "Create a new borrow record")
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        logger.debug("Creating new borrow request: {}", request);
        BorrowResponse response = borrowService.borrowBook(request);
        logger.debug("Created borrow: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Process book return")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long id) {
        logger.debug("Processing return for borrow ID: {}", id);
        BorrowResponse response = borrowService.returnBook(id);
        logger.debug("Processed return: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get borrowing history for a user")
    public ResponseEntity<Page<BorrowResponse>> getUserBorrows(
            @PathVariable Long userId,
            Pageable pageable) {
        logger.debug("Getting borrows for user ID: {} with pageable: {}", userId, pageable);
        Page<BorrowResponse> borrows = borrowService.getUserBorrows(userId, pageable);
        logger.debug("Found {} borrows for user {}", borrows.getTotalElements(), userId);
        return ResponseEntity.ok(borrows);
    }

    @GetMapping("/book/{bookId}")
    @Operation(summary = "Get borrowing history for a book")
    public ResponseEntity<Page<BorrowResponse>> getBookBorrows(
            @PathVariable Long bookId,
            Pageable pageable) {
        logger.debug("Getting borrows for book ID: {} with pageable: {}", bookId, pageable);
        Page<BorrowResponse> borrows = borrowService.getBookBorrows(bookId, pageable);
        logger.debug("Found {} borrows for book {}", borrows.getTotalElements(), bookId);
        return ResponseEntity.ok(borrows);
    }

    @GetMapping("/overdue")
    @Operation(summary = "List overdue borrowings")
    public ResponseEntity<Page<BorrowResponse>> getOverdueBorrows(Pageable pageable) {
        logger.debug("Getting overdue borrows with pageable: {}", pageable);
        Page<BorrowResponse> borrows = borrowService.getOverdueBorrows(pageable);
        logger.debug("Found {} overdue borrows", borrows.getTotalElements());
        return ResponseEntity.ok(borrows);
    }

    @PostMapping("/{id}/extend")
    @Operation(summary = "Extend due date")
    public ResponseEntity<BorrowResponse> extendBorrow(@PathVariable Long id) {
        logger.debug("Extending borrow ID: {}", id);
        BorrowResponse response = borrowService.extendBorrow(id);
        logger.debug("Extended borrow: {}", response);
        return ResponseEntity.ok(response);
    }
} 
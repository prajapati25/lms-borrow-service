package com.bits.borrowservice.service;

import com.bits.borrowservice.client.BookServiceClient;
import com.bits.borrowservice.client.UserServiceClient;
import com.bits.borrowservice.dto.request.BorrowRequest;
import com.bits.borrowservice.dto.response.BorrowResponse;
import com.bits.borrowservice.entity.Borrow;
import com.bits.borrowservice.entity.BorrowExtension;
import com.bits.borrowservice.entity.Fine;
import com.bits.borrowservice.entity.Return;
import com.bits.borrowservice.event.BorrowEventPublisher;
import com.bits.borrowservice.exception.*;
import com.bits.borrowservice.repository.BorrowExtensionRepository;
import com.bits.borrowservice.repository.BorrowRepository;
import com.bits.borrowservice.repository.FineRepository;
import com.bits.borrowservice.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {
    private static final Logger logger = LoggerFactory.getLogger(BorrowService.class);
    
    private final BorrowRepository borrowRepository;
    private final ReturnRepository returnRepository;
    private final FineRepository fineRepository;
    private final BorrowExtensionRepository borrowExtensionRepository;
    private final BookServiceClient bookServiceClient;
    private final UserServiceClient userServiceClient;
    private final BorrowEventPublisher eventPublisher;

    @Value("${borrow.default-loan-days}")
    private int defaultLoanDays;

    @Value("${borrow.extension-days}")
    private int extensionDays;

    @Value("${borrow.max-extensions}")
    private int maxExtensions;

    @Value("${borrow.fine-per-day}")
    private BigDecimal finePerDay;

    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {
        logger.debug("Processing borrow request: {}", request);
        
        // Validate user and book
        logger.debug("Checking user status for user ID: {}", request.getUserId());
        if (!userServiceClient.checkUserStatus(request.getUserId())) {
            logger.warn("User not active: {}", request.getUserId());
            throw new UserNotActiveException("User is not active");
        }

        logger.debug("Checking book availability for book ID: {}", request.getBookId());
        if (!bookServiceClient.checkBookAvailability(request.getBookId())) {
            logger.warn("Book not available: {}", request.getBookId());
            throw new BookNotAvailableException("Book is not available");
        }

        // Check if user has reached maximum active borrows
        long activeBorrows = borrowRepository.countActiveBorrowsByUserId(request.getUserId());
        logger.debug("User {} has {} active borrows", request.getUserId(), activeBorrows);
        if (activeBorrows >= 5) {
            logger.warn("Maximum borrows exceeded for user: {}", request.getUserId());
            throw new MaximumBorrowsExceededException("User has reached maximum active borrows");
        }

        // Create borrow record
        logger.debug("Creating borrow record for user {} and book {}", request.getUserId(), request.getBookId());
        Borrow borrow = new Borrow();
        borrow.setUserId(request.getUserId());
        borrow.setBookId(request.getBookId());
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(LocalDateTime.now().plusDays(defaultLoanDays));
        borrow.setStatus(Borrow.BorrowStatus.BORROWED);

        borrow = borrowRepository.save(borrow);
        logger.debug("Created borrow record: {}", borrow);

        // Update book status
        logger.debug("Updating book status to BORROWED for book ID: {}", request.getBookId());
        bookServiceClient.updateBookStatus(request.getBookId(), "BORROWED");

        // Publish event
        logger.debug("Publishing borrow event for borrow ID: {}", borrow.getId());
        eventPublisher.publishBorrowEvent(borrow);

        return BorrowResponse.fromEntity(borrow);
    }

    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        logger.debug("Processing return for borrow ID: {}", borrowId);
        
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> {
                    logger.warn("Borrow not found: {}", borrowId);
                    return new BorrowNotFoundException("Borrow record not found");
                });

        if (borrow.getStatus() != Borrow.BorrowStatus.BORROWED) {
            logger.warn("Invalid borrow status for return: {}", borrow.getStatus());
            throw new InvalidBorrowStatusException("Can only return borrowed books");
        }

        // Create return record
        logger.debug("Creating return record for borrow ID: {}", borrowId);
        Return returnRecord = new Return();
        returnRecord.setBorrow(borrow);
        returnRecord.setReturnDate(LocalDateTime.now());
        returnRepository.save(returnRecord);

        // Update borrow status
        logger.debug("Updating borrow status to RETURNED for borrow ID: {}", borrowId);
        borrow.setStatus(Borrow.BorrowStatus.RETURNED);
        borrow = borrowRepository.save(borrow);

        // Update book status
        logger.debug("Updating book status to AVAILABLE for book ID: {}", borrow.getBookId());
        bookServiceClient.updateBookStatus(borrow.getBookId(), "AVAILABLE");

        // Calculate fine if overdue
        if (borrow.getDueDate().isBefore(LocalDateTime.now())) {
            logger.debug("Calculating fine for overdue return");
            long daysOverdue = java.time.Duration.between(borrow.getDueDate(), LocalDateTime.now()).toDays();
            BigDecimal fineAmount = finePerDay.multiply(BigDecimal.valueOf(daysOverdue));
            
            Fine fine = new Fine();
            fine.setBorrow(borrow);
            fine.setAmount(fineAmount);
            fine.setStatus(Fine.FineStatus.PENDING);
            fineRepository.save(fine);
            logger.debug("Created fine record: {}", fine);
        }

        // Publish event
        logger.debug("Publishing return event for borrow ID: {}", borrowId);
        eventPublisher.publishReturnEvent(borrow);

        return BorrowResponse.fromEntity(borrow);
    }

    @Transactional
    public BorrowResponse extendBorrow(Long borrowId) {
        logger.debug("Processing extension for borrow ID: {}", borrowId);
        
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> {
                    logger.warn("Borrow not found: {}", borrowId);
                    return new BorrowNotFoundException("Borrow record not found");
                });

        if (borrow.getStatus() != Borrow.BorrowStatus.BORROWED) {
            logger.warn("Invalid borrow status for extension: {}", borrow.getStatus());
            throw new InvalidBorrowStatusException("Can only extend active borrows");
        }

        // Check if maximum extensions reached
        long extensionCount = borrowExtensionRepository.countExtensionsByBorrowId(borrowId);
        logger.debug("Current extension count for borrow {}: {}", borrowId, extensionCount);
        if (extensionCount >= maxExtensions) {
            logger.warn("Maximum extensions reached for borrow: {}", borrowId);
            throw new MaximumExtensionsExceededException("Maximum extensions reached");
        }

        // Create extension record
        logger.debug("Creating extension record for borrow ID: {}", borrowId);
        BorrowExtension extension = new BorrowExtension();
        extension.setBorrow(borrow);
        extension.setExtendedDays(extensionDays);
        extension.setNewDueDate(borrow.getDueDate().plusDays(extensionDays));
        borrowExtensionRepository.save(extension);

        // Update due date
        logger.debug("Updating due date for borrow ID: {}", borrowId);
        borrow.setDueDate(extension.getNewDueDate());
        borrow = borrowRepository.save(borrow);

        // Publish event
        logger.debug("Publishing due date event for borrow ID: {}", borrowId);
        eventPublisher.publishDueDateEvent(borrow);

        return BorrowResponse.fromEntity(borrow);
    }

    public Page<BorrowResponse> getUserBorrows(Long userId, Pageable pageable) {
        logger.debug("Getting borrows for user ID: {} with pageable: {}", userId, pageable);
        Page<BorrowResponse> borrows = borrowRepository.findByUserId(userId, pageable)
                .map(BorrowResponse::fromEntity);
        logger.debug("Found {} borrows for user {}", borrows.getTotalElements(), userId);
        return borrows;
    }

    public Page<BorrowResponse> getBookBorrows(Long bookId, Pageable pageable) {
        logger.debug("Getting borrows for book ID: {} with pageable: {}", bookId, pageable);
        Page<BorrowResponse> borrows = borrowRepository.findByBookId(bookId, pageable)
                .map(BorrowResponse::fromEntity);
        logger.debug("Found {} borrows for book {}", borrows.getTotalElements(), bookId);
        return borrows;
    }

    public Page<BorrowResponse> getOverdueBorrows(Pageable pageable) {
        logger.debug("Getting overdue borrows with pageable: {}", pageable);
        Page<BorrowResponse> borrows = borrowRepository.findOverdueBorrows(LocalDateTime.now(), pageable)
                .map(BorrowResponse::fromEntity);
        logger.debug("Found {} overdue borrows", borrows.getTotalElements());
        return borrows;
    }

    public Page<BorrowResponse> getAllBorrows(Pageable pageable) {
        logger.debug("Getting all borrows with pageable: {}", pageable);
        Page<BorrowResponse> borrows = borrowRepository.findAll(pageable)
                .map(BorrowResponse::fromEntity);
        logger.debug("Found {} total borrows", borrows.getTotalElements());
        return borrows;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void checkOverdueBorrows() {
        logger.debug("Running scheduled check for overdue borrows");
        List<Borrow> overdueBorrows = borrowRepository.findOverdueBorrows(LocalDateTime.now());
        logger.debug("Found {} overdue borrows", overdueBorrows.size());
        
        for (Borrow borrow : overdueBorrows) {
            logger.debug("Processing overdue borrow ID: {}", borrow.getId());
            borrow.setStatus(Borrow.BorrowStatus.OVERDUE);
            borrowRepository.save(borrow);
            eventPublisher.publishDueDateEvent(borrow);
        }
    }
} 
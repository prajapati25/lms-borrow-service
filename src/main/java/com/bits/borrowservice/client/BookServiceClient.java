package com.bits.borrowservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class BookServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(BookServiceClient.class);
    private final RestTemplate restTemplate;

    @Value("${service.book.url}")
    private String bookServiceUrl;

    @Value("${service.book.timeout:5000}")
    private int timeout;

    @Value("${service.book.retry-attempts:3}")
    private int retryAttempts;

    @Value("${service.book.retry-delay:1000}")
    private int retryDelay;

    public BookServiceClient(@Qualifier("bookServiceRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "checkAvailabilityFallback")
    public boolean checkBookAvailability(Long bookId) {
        logger.debug("Checking availability for book ID: {}", bookId);
        String url = bookServiceUrl + "/api/books/" + bookId + "/availability";
        logger.debug("Calling book service at URL: {}", url);
        
        try {
            boolean isAvailable = restTemplate.getForObject(url, Boolean.class);
            logger.debug("Book availability check result for book ID {}: {}", bookId, isAvailable);
            return isAvailable;
        } catch (Exception e) {
            logger.error("Error checking book availability for book ID {}: {}", bookId, e.getMessage(), e);
            throw e;
        }
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "updateStatusFallback")
    public void updateBookStatus(Long bookId, String status) {
        logger.debug("Updating status for book ID: {} to status: {}", bookId, status);
        String url = bookServiceUrl + "/api/books/" + bookId + "/status";
        logger.debug("Calling book service at URL: {}", url);
        
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("status", status);

            restTemplate.put(url, requestBody);
            logger.debug("Successfully updated book status for book ID {} to {}", bookId, status);
        } catch (Exception e) {
            logger.error("Error updating book status for book ID {}: {}", bookId, e.getMessage(), e);
            throw e;
        }
    }

    private boolean checkAvailabilityFallback(Long bookId, Exception e) {
        logger.warn("Circuit breaker triggered for checkBookAvailability. Book ID: {}. Error: {}", bookId, e.getMessage());
        // In case of service unavailability, assume book is not available
        return false;
    }

    private void updateStatusFallback(Long bookId, String status, Exception e) {
        logger.warn("Circuit breaker triggered for updateBookStatus. Book ID: {}, Status: {}. Error: {}", 
            bookId, status, e.getMessage());
        // Log the error but don't throw exception
        // The transaction will be rolled back if needed
    }
} 
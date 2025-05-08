package com.bits.borrowservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    private final RestTemplate restTemplate;

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Value("${service.user.timeout:5000}")
    private int timeout;

    @Value("${service.user.retry-attempts:3}")
    private int retryAttempts;

    @Value("${service.user.retry-delay:1000}")
    private int retryDelay;

    public UserServiceClient(@Qualifier("userServiceRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "validateTokenFallback")
    public boolean validateToken(String token) {
        logger.debug("Validating token");
        String url = userServiceUrl + "/api/auth/validate";
        logger.debug("Calling user service at URL: {}", url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            Boolean isValid = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Boolean.class
            ).getBody();
            
            logger.debug("Token validation result: {}", isValid);
            return Boolean.TRUE.equals(isValid);
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean validateTokenFallback(String token, Exception e) {
        logger.warn("Circuit breaker triggered for validateToken. Error: {}", e.getMessage());
        // In case of service unavailability, reject the token
        return false;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "checkUserStatusFallback")
    public boolean checkUserStatus(Long userId) {
        logger.debug("Checking status for user ID: {}", userId);
        String url = userServiceUrl + "/api/users/" + userId + "/status";
        logger.debug("Calling user service at URL: {}", url);
        
        try {
            boolean isActive = restTemplate.getForObject(url, Boolean.class);
            logger.debug("User status check result for user ID {}: {}", userId, isActive);
            return isActive;
        } catch (Exception e) {
            logger.error("Error checking user status for user ID {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private boolean checkUserStatusFallback(Long userId, Exception e) {
        logger.warn("Circuit breaker triggered for checkUserStatus. User ID: {}. Error: {}", userId, e.getMessage());
        // In case of service unavailability, assume user is not active
        return false;
    }
} 
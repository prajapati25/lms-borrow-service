package com.bits.borrowservice.config;

import com.bits.borrowservice.client.BookServiceClient;
import com.bits.borrowservice.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("dev")
public class DevConfig {

    @Bean
    public UserServiceClient userServiceClient(@Qualifier("userServiceRestTemplate") RestTemplate restTemplate) {
        return new UserServiceClient(restTemplate) {
            @Override
            public boolean validateToken(String token) {
                // For development, accept any token that starts with "valid-"
                if (token == null || !token.startsWith("valid-")) {
                    return false;
                }

                return true;
            }

            @Override
            public boolean checkUserStatus(Long userId) {
                // For development, assume all users are active
                return true;
            }
        };
    }
    @Bean
    public BookServiceClient bookServiceClient(@Qualifier("bookServiceRestTemplate") RestTemplate restTemplate) {
        return new BookServiceClient(restTemplate) {
            @Override
            public boolean checkBookAvailability(Long bookId) {
              return true;
            }

            @Override
            public void updateBookStatus(Long bookId, String status) {
            }
        };
    }
} 
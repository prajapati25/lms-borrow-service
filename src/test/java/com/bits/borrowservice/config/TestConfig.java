package com.bits.borrowservice.config;

import com.bits.borrowservice.client.UserServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public UserServiceClient mockUserServiceClient() {
        return new UserServiceClient(null) {
            @Override
            public boolean validateToken(String token) {
                // For testing, accept any token that starts with "valid-"
                return token != null && token.startsWith("valid-");
            }

            @Override
            public boolean checkUserStatus(Long userId) {
                // For testing, assume all users are active
                return true;
            }
        };
    }
} 
package com.bits.borrowservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Value("${service.book.timeout:5000}")
    private int bookServiceTimeout;

    @Value("${service.user.timeout:5000}")
    private int userServiceTimeout;

    @Bean
    public RestTemplate bookServiceRestTemplate() {
        logger.debug("Creating RestTemplate for Book Service with timeout: {}ms", bookServiceTimeout);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(bookServiceTimeout);
        factory.setReadTimeout(bookServiceTimeout);
        return new RestTemplate(factory);
    }

    @Bean
    public RestTemplate userServiceRestTemplate() {
        logger.debug("Creating RestTemplate for User Service with timeout: {}ms", userServiceTimeout);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(userServiceTimeout);
        factory.setReadTimeout(userServiceTimeout);
        return new RestTemplate(factory);
    }
} 
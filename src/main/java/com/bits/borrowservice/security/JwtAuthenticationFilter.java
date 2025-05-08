package com.bits.borrowservice.security;

import com.bits.borrowservice.client.UserServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserServiceClient userServiceClient;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // List of paths that should bypass authentication
    private static final String[] PUBLIC_PATHS = {
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api-docs/**"

    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // More robust pattern matching
        boolean patternMatch = false;
        for (String pattern : PUBLIC_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                patternMatch = true;
                break;
            }
        }
        log.info("MATCHING: patternMatch={}", patternMatch);

        return patternMatch;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        try {
            String jwt = getJwtFromRequest(request);

            // Only require token for non-public paths
            if (!StringUtils.hasText(jwt)) {
                log.warn("No token provided for path: {}", path);
                throw new BadCredentialsException("No token provided");
            }

            log.info("Validating token for path: {}", path);
            if (!userServiceClient.validateToken(jwt)) {
                log.warn("Invalid token for path: {}", path);
                throw new BadCredentialsException("Invalid token");
            }

            log.info("Token valid, loading user details");
            UserDetails userDetails = userDetailsService.loadUserByUsername("authenticated-user");
            var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication successful, continuing filter chain");
            filterChain.doFilter(request, response);

        } catch (AuthenticationException ex) {
            log.error("Authentication error for path {}: {}", path, ex.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("path", request.getRequestURI());
            errorDetails.put("timestamp", LocalDateTime.now());

            objectMapper.writeValue(response.getOutputStream(), errorDetails);
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
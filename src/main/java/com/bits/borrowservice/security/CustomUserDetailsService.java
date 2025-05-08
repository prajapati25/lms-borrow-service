package com.bits.borrowservice.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In a microservice architecture, we trust the user service's token validation
        // So we just need to create a user with appropriate authorities
        return new User(
            username,
            "", // No password needed as we trust the token
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
} 
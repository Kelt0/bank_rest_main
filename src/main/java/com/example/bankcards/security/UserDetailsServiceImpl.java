package com.example.bankcards.security;

import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository USER_REPOSITORY;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.USER_REPOSITORY = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return USER_REPOSITORY.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found with username: " + username));
    }
}

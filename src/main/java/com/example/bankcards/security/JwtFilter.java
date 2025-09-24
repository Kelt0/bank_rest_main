package com.example.bankcards.security;

import com.example.bankcards.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil JWT_UTIL;
    private final UserDetailsService USER_DETAILS_SERVICE;
    private final JwtProperties JWT_PROPERTIES;

    public JwtFilter(JwtUtil JWT_UTIL, UserDetailsService USER_DETAILS_SERVICE, JwtProperties JWT_PROPERTIES) {
        this.JWT_UTIL = JWT_UTIL;
        this.USER_DETAILS_SERVICE = USER_DETAILS_SERVICE;
        this.JWT_PROPERTIES = JWT_PROPERTIES;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = parseJwt(request);

        if(jwt != null && JWT_UTIL.validateToken(jwt)){
            String username = JWT_UTIL.getUsernameFromJwt(jwt);
            UserDetails userDetails = USER_DETAILS_SERVICE.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);

    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(JWT_PROPERTIES.getHeader());
        String prefix = JWT_PROPERTIES.getPrefix() + " ";

        if (headerAuth != null && headerAuth.startsWith(prefix)){
            return headerAuth.substring(prefix.length());
        }

        return null;
    }
}

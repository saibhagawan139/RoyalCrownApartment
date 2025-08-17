//
//package com.royalcrown.config;
//
//import com.royalcrown.service.ApartmentSecurityService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Collections;
//
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
//
//    private final JwtProvider jwtProvider;
//    private final ApartmentSecurityService apartmentService;
//
//    public JwtAuthFilter(JwtProvider jwtProvider, ApartmentSecurityService apartmentService) {
//        this.jwtProvider = jwtProvider;
//        this.apartmentService = apartmentService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req,
//                                    HttpServletResponse res,
//                                    FilterChain chain) throws ServletException, IOException {
//
//        final String authHeader = req.getHeader("Authorization");
//        String token = null;
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            token = authHeader.substring(7);
//        }
//
//        if (token != null && jwtProvider.validate(token)) {
//            String username = jwtProvider.getUsername(token);
//            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                apartmentService.findByUsername(username).ifPresent(user -> {
//                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
//                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                            user, null, Collections.singletonList(authority));
//                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//                    logger.info("Authorities for user {}: {}", username, auth.getAuthorities());
//
//                });
//            }
//        }
//
//        chain.doFilter(req, res);
//    }
//}
//

package com.royalcrown.config;

import com.royalcrown.service.ApartmentSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtProvider jwtProvider;
    private final ApartmentSecurityService apartmentService;

    public JwtAuthFilter(JwtProvider jwtProvider, ApartmentSecurityService apartmentService) {
        this.jwtProvider = jwtProvider;
        this.apartmentService = apartmentService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = req.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && jwtProvider.validate(token)) {
            String username = jwtProvider.getUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                apartmentService.findByUsername(username).ifPresent(user -> {
                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null,
                                    Collections.singletonList(authority));

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // 🔎 Added logger here
                    logger.info("Authorities for user {}: {}", username, auth.getAuthorities());
                });
            }
        }

        chain.doFilter(req, res);
    }
}

package com.royalcrown.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Add CorsConfigurationSource bean and enable cors() in the filter chain
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
            "http://royalcrownapartmentsecurity.s3-website.ap-south-1.amazonaws.com"
            // add other allowed origins here if needed (e.g. local dev)
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .cors() // enable CORS support using corsConfigurationSource()
            .and()
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                .requestMatchers("/auth/login", "/auth/register", "/auth/register-batch").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("PRESIDENT", "ADMIN")
                .requestMatchers("/security/visits").hasAnyRole("PRESIDENT", "ADMIN")
                .requestMatchers("/otp/generate").hasAnyRole("OWNER", "TENANT")
                .requestMatchers("/otp/validate").hasAnyRole("SECURITY_GUARD")
                .requestMatchers("/auth/me", "/auth/logout", "/auth/change-password").authenticated()
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

/*
 * This is the working code commented for adding new code for CORS
 */
//@Configuration
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    private final JwtProvider jwtProvider;
//
//    public SecurityConfig(JwtProvider jwtProvider) {
//        this.jwtProvider = jwtProvider;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(auth -> auth
//            	.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//            	.requestMatchers(HttpMethod.DELETE, "/**").permitAll()
//            	.requestMatchers(HttpMethod.POST, "/**").permitAll()
//                .requestMatchers("/auth/login", "/auth/register", "/auth/register-batch").permitAll()
//                .requestMatchers("/admin/**").hasAnyRole("PRESIDENT", "ADMIN")
////                .requestMatchers("/security/visitsData").hasAnyRole("PRESIDENT", "ADMIN","SECURITY_GUARD")
//                .requestMatchers("/security/visits").hasAnyRole("PRESIDENT", "ADMIN")
//                .requestMatchers("/otp/generate").hasAnyRole("OWNER", "TENANT")
//                .requestMatchers("/otp/validate").hasAnyRole("SECURITY_GUARD")
//                .requestMatchers("/auth/me", "/auth/logout", "/auth/change-password").authenticated()
//                .anyRequest().denyAll()
//            )
//            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}

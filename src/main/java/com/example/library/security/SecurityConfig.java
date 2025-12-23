package com.example.library.security;

import com.example.library.api.dto.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Actuator & docs
                .requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Allow CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", objectMapper))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", objectMapper))
            );

        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message, ObjectMapper objectMapper) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        BaseResponse<Void> body = BaseResponse.<Void>builder()
                .responseMessage(message)
                .build();
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();

        UserDetails member = User.withUsername("member")
                .password("member123")
                .roles("MEMBER")
                .build();

        return new InMemoryUserDetailsManager(admin, member);
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        // For a take-home assignment, in-memory + basic auth is enough.
        // In production, use BCryptPasswordEncoder and a real identity provider.
        return NoOpPasswordEncoder.getInstance();
    }
}

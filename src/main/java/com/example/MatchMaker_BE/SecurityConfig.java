package com.example.MatchMaker_BE;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeRequests(authorizeRequests ->
                    authorizeRequests
                            .anyRequest().authenticated()
            )
                    .cors().configurationSource(request -> {
                        CorsConfiguration cors = new CorsConfiguration();
                        cors.setAllowedOrigins(List.of("http://localhost:8080"));
                            cors.setAllowedMethods(List.of("*"));
                            cors.addAllowedHeader("*");
                            return cors;
                    });
            http.csrf().disable();
        return http.build();
    }
}
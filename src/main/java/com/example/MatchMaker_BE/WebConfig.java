package com.example.MatchMaker_BE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Erlaube Anfragen von allen Ursprüngen
        config.addAllowedOrigin("http://localhost:8080");

        // Erlaube bestimmte HTTP-Methoden (GET, POST, etc.)
        config.addAllowedMethod("*");

        // Erlaube bestimmte HTTP-Header
        config.addAllowedHeader("*");

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

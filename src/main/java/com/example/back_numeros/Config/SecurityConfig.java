package com.example.back_numeros.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactivamos CSRF para poder hacer POST/PUT después sin problemas
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // <-- LA MAGIA: Permite absolutamente todo sin tokens ni contraseñas
                );
        return http.build();
    }
}
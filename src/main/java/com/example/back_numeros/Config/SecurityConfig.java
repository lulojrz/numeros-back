package com.example.back_numeros.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
      @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // 1. LE AVISAMOS A SPRING SECURITY QUE USE TU CONFIGURACIÓN DE CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Desactivamos CSRF para poder hacer POST/PUT
            .csrf(csrf -> csrf.disable())
            
            // 3. Gestión de sesiones
            .sessionManagement(session -> session
                    .sessionFixation(sessionFixation -> sessionFixation.newSession())
            )
            
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/usuarios/login","/api/numeros").permitAll()
                    .requestMatchers("/api/editar/{id}","/usuarios/cambiar-contrasena", "/usuarios","/experiencias/traer","/experiencias/agregar").authenticated()
                    .requestMatchers( "/usuarios/borrar/{id}").hasAuthority("ROLE_ANC")
                    .requestMatchers( "/usuarios/crear", "/api/borrar/{id}", "/api/agregar","/api/turnos/puntos").hasAnyAuthority("ROLE_ANC", "ROLE_SM")
                    .anyRequest().authenticated()
            );
            
    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Le habilitamos la entrada exacta a tu puerto de React
        configuration.setAllowedOrigins(List.of("https://colegiales.netlify.app"));

        // Permitimos los métodos que vas a usar en tus contextos de React
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permitimos que viajen headers comunes (como el Content-Type para los JSON)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica para todos los endpoints del back
        return source;
    }
}

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
                        // Permitir preflights
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Públicos (Login y ver números disponibles)
                        .requestMatchers("/usuarios/login", "/api/numeros").permitAll()

                        // Acciones de cualquier usuario AUTENTICADO (incluye anotarse en turnos y ver la semana)
                        .requestMatchers(
                                "/api/editar/{id}",
                                "/usuarios/cambiar-contrasena",
                                "/usuarios",
                                "/experiencias/traer",
                                "/experiencias/agregar",
                                "/api/turnos/semana",          // <-- Agregado para orden
                                "/api/turnos/anotarse/{id}"    // <-- Agregado para orden
                        ).authenticated()

                        // Exclusivo Administrador Supremo (ANC)
                        .requestMatchers("/usuarios/borrar/{id}").hasAuthority("ROLE_ANC")

                        // Exclusivo Administradores y Moderadores (ANC, SM)
                        .requestMatchers(
                                "/usuarios/crear",
                                "/api/borrar/{id}",
                                "/api/agregar",
                                "/api/turnos/puntos",
                                "/api/turnos/puntos/{id}",     // <-- Corregido: Agregada la barra "/" inicial
                                "/api/turnos/generar" ,
                                "/api/turnos/crear",
                                "/api/turnos/obtener",
                                "/api/turnos/eliminar/{id}"

                                // <-- Agregado: Ahora protegido para que no lo use cualquiera
                        ).hasAnyAuthority("ROLE_ANC", "ROLE_SM")

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

        // Le habilitamos la entrada exacta a tu puerto de Netlify
        configuration.setAllowedOrigins(List.of("https://colegiales.netlify.app"));

        // Permitimos los métodos que vas a usar
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permitimos cabeceras estándar completas para evitar bloqueos del navegador
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "Accept", "Origin"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica para todos los endpoints del back
        return source;
    }
}
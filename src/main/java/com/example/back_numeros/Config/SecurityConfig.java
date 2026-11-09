package com.example.back_numeros.Config;

import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Usuario;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.core.Ordered;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.text.Normalizer;
import java.util.Set;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String PRODUCTION_ORIGIN = "https://colegiales.netlify.app";

    // En Railway, CORS_ORIGINS puede contener orígenes adicionales separados por comas.
    // En local configurar: CORS_ORIGINS=http://localhost:5173

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioRepository usuarioRepository) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.sameSite("None").secure(true));
        AuthorizationManager<RequestAuthorizationContext> ancOrSupport =
                usuarioAccess(usuarioRepository, Set.of("ROLE_ANC"));
        AuthorizationManager<RequestAuthorizationContext> ancSmOrSupport =
                usuarioAccess(usuarioRepository, Set.of("ROLE_ANC", "ROLE_SM"));

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.newSession())
                )
                .logout(logout -> logout
                        .logoutUrl("/usuarios/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/csrf", "/territorios/traer").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/cambiar-contrasena").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/crear").access(ancOrSupport)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/editar/**").access(ancOrSupport)
                        .requestMatchers(HttpMethod.DELETE, "/**").access(ancOrSupport)
                        .requestMatchers(
                                "/api/agregar",
                                "/api/turnos/puntos",
                                "/api/turnos/puntos/{id}",
                                "/api/turnos/generar",
                                "/api/turnos/crear",
                                "/api/turnos/obtener",
                                "/api/turnos/todos",
                                "/reporte/traer",
                                "/campanas/agregar",
                                "/campanas/editar/{id}"
                        ).access(ancSmOrSupport)
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> usuarioAccess(
            UsuarioRepository usuarioRepository, Set<String> privilegiosPermitidos) {
        return (authentication, context) -> {
            Authentication currentUser = authentication.get();
            if (currentUser == null || !currentUser.isAuthenticated()) {
                return new AuthorizationDecision(false);
            }

            boolean autorizado = usuarioRepository.findByUsuario(currentUser.getName())
                    .map(usuario -> privilegiosPermitidos.contains(usuario.getPrivilegio())
                            || esSoporte(usuario))
                    .orElse(false);
            return new AuthorizationDecision(autorizado);
        };
    }

    private boolean esSoporte(Usuario usuario) {
        String asignacion = usuario.getAsignacion();
        if (asignacion == null) return false;
        String normalizada = Normalizer.normalize(asignacion, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toLowerCase(Locale.ROOT);
        return normalizada.equals("soporte");
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(
            CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> registration =
                new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        registration.setName("corsFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ORIGINS:}") String corsOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Stream.concat(
                        Stream.of(PRODUCTION_ORIGIN),
                        Arrays.stream(corsOrigins.split(",")))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

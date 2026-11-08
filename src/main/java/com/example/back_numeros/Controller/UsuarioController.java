package com.example.back_numeros.Controller;


import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.Config.LoginAttemptService;
import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.example.back_numeros.model.cambiarContrasenaDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("usuarios")

public class UsuarioController {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    LoginAttemptService loginAttemptService;

    @GetMapping()
    public List<Usuario> traerUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return usuarioRepository.findAll(pageable).getContent();
    }
    @PutMapping("editar/{id}")
    public ResponseEntity<?> editarUsuario(@RequestBody Usuario usuario_a_editar, @PathVariable Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setPrivilegio(usuario_a_editar.getPrivilegio());
            usuario.setBanner(usuario_a_editar.getBanner());
            usuario.setAsignacion(usuario_a_editar.getAsignacion());
            usuario.setCarrito(usuario_a_editar.getCarrito());
            usuario.setTelefono(usuario_a_editar.getTelefono());
            usuario.setHabilitacionPublica(usuario_a_editar.getHabilitacionPublica());
            usuario.setGrupo(usuario_a_editar.getGrupo());
            usuario.setConductor(usuario_a_editar.getConductor());
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario_nuevo){
        String passwordplana = usuario_nuevo.getContrasena();

        String passwordHasheada = passwordEncoder.encode(passwordplana);
        usuario_nuevo.setContrasena(passwordHasheada);
        return usuarioRepository.save(usuario_nuevo);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest, HttpServletRequest request) {
        String address = "login:" + request.getRemoteAddr();
        if (loginAttemptService.isBlocked(address)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "900")
                    .body("Demasiados intentos. Intente nuevamente más tarde.");
        }

        Optional<Usuario> encontrado = loginRequest == null || loginRequest.getUsuario() == null
                ? Optional.empty()
                : usuarioRepository.findByUsuario(loginRequest.getUsuario());

        if (encontrado.isPresent() && loginRequest.getContrasena() != null
                && passwordEncoder.matches(loginRequest.getContrasena(), encontrado.get().getContrasena())) {
            Usuario user = encontrado.get();
            loginAttemptService.clear(address);

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getPrivilegio());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsuario(),
                    null,
                    Collections.singletonList(authority)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            return ResponseEntity.ok(user);
        } else {
            loginAttemptService.recordFailure(address);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PutMapping("cambiar-contrasena")
    public ResponseEntity<String> cambiarContrasena(
            @RequestBody cambiarContrasenaDTO datos,
            HttpServletRequest request) {
        String address = "password-change:" + request.getRemoteAddr();
        if (loginAttemptService.isBlocked(address)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "900")
                    .body("Demasiados intentos. Intente nuevamente más tarde.");
        }
        if (datos == null || datos.getUsuario() == null || datos.getContrasenaActual() == null
                || datos.getNuevaContrasena() == null || datos.getNuevaContrasena().isBlank()) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsuario(datos.getUsuario());
        if (usuarioOptional.isEmpty()) {
            loginAttemptService.recordFailure(address);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
        Usuario usuario = usuarioOptional.get();
        if (!passwordEncoder.matches(datos.getContrasenaActual(), usuario.getContrasena())) {
            loginAttemptService.recordFailure(address);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }

        usuario.setContrasena(passwordEncoder.encode(datos.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        loginAttemptService.clear(address);

        return ResponseEntity.ok("correcto");
    }

    @DeleteMapping("borrar/{id}")
    public ResponseEntity<?> borrarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioEliminado = usuarioRepository.findById(id);
        if (usuarioEliminado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.delete(usuarioEliminado.get());
        return ResponseEntity.ok(usuarioEliminado.get());
    }


}

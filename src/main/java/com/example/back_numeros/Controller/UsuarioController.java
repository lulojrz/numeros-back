package com.example.back_numeros.Controller;

import com.example.back_numeros.Config.JwtUtil;
import com.example.back_numeros.Config.LoginAttemptService;
import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Usuario;
import com.example.back_numeros.model.cambiarContrasenaDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("usuarios")
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    LoginAttemptService loginAttemptService;

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping()
    public List<Usuario> traerUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return usuarioRepository.findAll(pageable).getContent();
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> traerUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return usuarioRepository.findByUsuario(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PutMapping("editar/{id}")
    public ResponseEntity<?> editarUsuario(
            @RequestBody Usuario usuario_a_editar,
            @PathVariable Long id) {
        if (usuario_a_editar == null) {
            return ResponseEntity.badRequest().body("Datos de usuario inválidos");
        }

        return usuarioRepository.findById(id).map(usuario -> {
            String usuarioAnterior = usuario.getUsuario();
            String usuarioNuevo = usuario_a_editar.getUsuario();
            if (usuarioNuevo != null && !usuarioNuevo.isBlank() && !usuarioNuevo.equals(usuarioAnterior)) {
                Optional<Usuario> existente = usuarioRepository.findByUsuario(usuarioNuevo);
                if (existente.isPresent() && !existente.get().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso");
                }
                usuario.setUsuario(usuarioNuevo);
            }
            if (usuario_a_editar.getNombre() != null) {
                usuario.setNombre(usuario_a_editar.getNombre());
            }
            if (usuario_a_editar.getApellido() != null) {
                usuario.setApellido(usuario_a_editar.getApellido());
            }
            usuario.setPrivilegio(usuario_a_editar.getPrivilegio());
            usuario.setBanner(usuario_a_editar.getBanner());
            usuario.setAsignacion(usuario_a_editar.getAsignacion());
            usuario.setCarrito(usuario_a_editar.getCarrito());
            usuario.setTelefono(usuario_a_editar.getTelefono());
            usuario.setHabilitacionPublica(usuario_a_editar.getHabilitacionPublica());
            usuario.setGrupo(usuario_a_editar.getGrupo());
            usuario.setConductor(usuario_a_editar.getConductor());
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
            if (autenticacion != null && autenticacion.getName().equals(usuarioAnterior)
                    && !usuarioAnterior.equals(usuarioGuardado.getUsuario())) {
                String nuevoToken = jwtUtil.generarToken(usuarioGuardado.getUsuario(), usuarioGuardado.getPrivilegio());
                Map<String, Object> respuesta = construirRespuestaUsuario(usuarioGuardado, nuevoToken);
                return ResponseEntity.ok(respuesta);
            }

            return ResponseEntity.ok(usuarioGuardado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario_nuevo) {
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

            String token = jwtUtil.generarToken(user.getUsuario(), user.getPrivilegio());
            Map<String, Object> respuesta = construirRespuestaUsuario(user, token);

            return ResponseEntity.ok(respuesta);
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

    @GetMapping("/habilitados/publica")
    public List<Usuario> traerHabilitados() {
        return usuarioRepository.findByHabilitacionPublicaTrueOrderByApellidoAscNombreAsc();
    }

    private Map<String, Object> construirRespuestaUsuario(Usuario user, String token) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("id", user.getId());
        map.put("usuario", user.getUsuario());
        map.put("nombre", user.getNombre());
        map.put("apellido", user.getApellido());
        map.put("privilegio", user.getPrivilegio());
        map.put("banner", user.getBanner());
        map.put("asignacion", user.getAsignacion());
        map.put("carrito", user.getCarrito());
        map.put("telefono", user.getTelefono());
        map.put("habilitacionPublica", user.getHabilitacionPublica());
        map.put("grupo", user.getGrupo());
        map.put("conductor", user.getConductor());
        return map;
    }
}
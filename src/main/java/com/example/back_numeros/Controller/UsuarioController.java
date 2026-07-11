package com.example.back_numeros.Controller;


import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.back_numeros.model.cambiarContrasenaDTO;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("usuarios")
@CrossOrigin(origins = "https://colegiales.netlify.app")
public class UsuarioController {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping()
    public List<Usuario> traerUsuarios(){
        return usuarioRepository.findAll();
    }
    @PutMapping("editar/{id}")
    public Usuario editarUsuario(@RequestBody Usuario usuario_a_editar, @PathVariable Long id ){
       return  usuarioRepository.findById(id).map(
                usuario -> {

                        usuario.setPrivilegio(usuario_a_editar.getPrivilegio());
                return usuarioRepository.save(usuario);
                }


        ).orElse( null);

    }

    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario_nuevo){
        String passwordplana = usuario_nuevo.getContrasena();

        String passwordHasheada = passwordEncoder.encode(passwordplana);
        usuario_nuevo.setContrasena(passwordHasheada);
        return usuarioRepository.save(usuario_nuevo);

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario loginRequest) { //

        Optional<Usuario> encontrado = usuarioRepository.findByUsuario(loginRequest.getUsuario());

        if (encontrado.isPresent() && passwordEncoder.matches(loginRequest.getContrasena(), encontrado.get().getContrasena())) {
            return ResponseEntity.ok("correcto");
        } else {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PutMapping("cambiar-contrasena")
    public ResponseEntity<String> cambiarContrasena(@RequestBody cambiarContrasenaDTO datos) {

        // 1. Buscar el usuario en la base de datos
        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsuario(datos.getUsuario());

        // CORRECCIÓN CLAVE: El Optional nunca es null, se chequea con .isEmpty()
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        // Extraemos el usuario real del envoltorio para trabajar tranquilos
        Usuario usuario = usuarioOptional.get();

        // 2. Validar que la 'contrasenaActual' coincida con la de la BD
        if (!passwordEncoder.matches(datos.getContrasenaActual(), usuario.getContrasena())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Contraseña actual incorrecta");
        }

        // 3. Encriptar y guardar la nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(datos.getNuevaContrasena()));

        // CORRECCIÓN CLAVE: Ahora le pasamos el objeto 'usuario' (Tipo: Usuario), no el Optional
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("correcto");
    }

    @DeleteMapping("borrar/{id}")
    public Optional<Usuario> borrarUsuario(@PathVariable Long id){
        Optional<Usuario> usuarioEliminado = usuarioRepository.findById(id);
        usuarioRepository.deleteById(id);
        return usuarioEliminado;

    }


}

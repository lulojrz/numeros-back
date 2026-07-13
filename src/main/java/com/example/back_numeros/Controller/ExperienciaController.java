package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.ExperienciaRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Experiencia;
import com.example.back_numeros.model.Numero;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/experiencias")
@CrossOrigin(origins = "https://colegiales.netlify.app")
public class ExperienciaController {
    @Autowired
    ExperienciaRepository experienciaRepository;
    @Autowired
    UsuarioRepository usuarioRepository;


    @GetMapping("/traer")
    public List<Experiencia> traerExperiencias(){
        return experienciaRepository.findAll();

    }
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarExperiencia(@RequestBody Experiencia experiencia) {
        if (experiencia == null) {
            return ResponseEntity.badRequest().body("La experiencia no puede ser nula");
        }

        // 1. Buscamos al usuario en la base de datos
        Optional<Usuario> userOptional = usuarioRepository.findByUsuario(experiencia.getUsuario().getUsuario());

        // 2. Verificamos si el usuario REALMENTE existe usando .isPresent()
        if (userOptional.isPresent()) {
            // Extraemos el objeto Usuario real del contenedor
            Usuario usuarioReal = userOptional.get();

            // Se lo vinculamos a la experiencia
            experiencia.setUsuario(usuarioReal);

            // Guardamos en la base de datos
            Experiencia guardada = experienciaRepository.save(experiencia);
            return ResponseEntity.ok(guardada);
        } else {
            // Si no existe el usuario, devolvemos un error limpio en vez de explotar con un 500
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado en el sistema");
        }
    }
}

package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.ExperienciaRepository;
import com.example.back_numeros.Repository.ReaccionExperienciaRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Experiencia;
import com.example.back_numeros.model.Numero;
import com.example.back_numeros.model.ReaccionExperiencia;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/experiencias")

public class ExperienciaController {
    @Autowired
    ExperienciaRepository experienciaRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    ReaccionExperienciaRepository reaccionExperienciaRepository;


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
    @PostMapping("/{id}/reaccionar")
    public ResponseEntity<?> reaccionar(
            @PathVariable Long id,
            @RequestParam String tipo,
            @RequestParam String usuario) {

        // 1. Buscar la experiencia por 'id'
        Optional<Experiencia> expOpt = experienciaRepository.findById(id);
        if (expOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Experiencia experiencia = expOpt.get();
        // Buscar al usuario que está reaccionando
        Optional<Usuario> userOpt = usuarioRepository.findByUsuario(usuario);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        Usuario user = userOpt.get();
        // 2. Verificar si ese 'usuario' ya reaccionó con ese 'tipo' en esa experiencia
        List<ReaccionExperiencia> reacciones = experiencia.getReacciones_list();
        Optional<ReaccionExperiencia> reaccionExistente = Optional.empty();
        if (reacciones != null) {
            reaccionExistente = reacciones.stream()
                    .filter(r -> r.getUsuario() != null && r.getUsuario().getUsuario().equals(usuario) && r.getTipo().equals(tipo))
                    .findFirst();
        }
        if (reaccionExistente.isPresent()) {
            // 4. Si ya existe, la eliminamos (funciona como un botón de "Quitar Me Gusta")
            reaccionExperienciaRepository.delete(reaccionExistente.get());
        } else {
            // 3. Si no existe, crear la ReaccionExperiencia y guardarla
            ReaccionExperiencia nuevaReaccion = new ReaccionExperiencia();
            nuevaReaccion.setExperiencia(experiencia);
            nuevaReaccion.setUsuario(user);
            nuevaReaccion.setTipo(tipo);
            reaccionExperienciaRepository.save(nuevaReaccion);
        }

        return ResponseEntity.ok().build();
    }
}

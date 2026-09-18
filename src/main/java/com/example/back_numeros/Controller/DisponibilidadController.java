package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.DisponibilidadRepository;
import com.example.back_numeros.model.Disponibilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disponibilidades")
public class DisponibilidadController {

    @Autowired
    DisponibilidadRepository disponibilidadRepository;

    @GetMapping("/traer")
    public List<Disponibilidad> traerTodas() {
        return disponibilidadRepository.findAll();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Disponibilidad> traerPorUsuario(@PathVariable Long usuarioId) {
        return disponibilidadRepository.findByUsuarioId(usuarioId);
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarDisponibilidad(@RequestBody Disponibilidad disponibilidad) {
        if (disponibilidad == null || disponibilidad.getUsuario() == null) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }
        return ResponseEntity.ok(disponibilidadRepository.save(disponibilidad));
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarDisponibilidad(@PathVariable Long id) {
        if (disponibilidadRepository.existsById(id)) {
            disponibilidadRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

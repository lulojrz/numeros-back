package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.CampanaRepository;
import com.example.back_numeros.model.Campana;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/campanas")
public class CampanaController {

    @Autowired
    CampanaRepository campanaRepository;

    @GetMapping("/traer")
    public List<Campana> traerCampanas() {
        return campanaRepository.findAll();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarCampana(@RequestBody Campana campana) {
        if (campana == null) {
            return ResponseEntity.badRequest().body("La campaña no puede ser nula");
        }
        Campana guardada = campanaRepository.save(campana);
        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarCampana(@PathVariable Long id) {
        Optional<Campana> campana = campanaRepository.findById(id);
        if (campana.isPresent()) {
            campanaRepository.deleteById(id);
            return ResponseEntity.ok(campana.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarCampana(@PathVariable Long id, @RequestBody Campana campanaActualizada) {
        return campanaRepository.findById(id).map(campana -> {
            campana.setTitulo(campanaActualizada.getTitulo());
            campana.setFechaInicio(campanaActualizada.getFechaInicio());
            campana.setFechaFin(campanaActualizada.getFechaFin());
            campana.setEstado(campanaActualizada.getEstado());
            campana.setImagen(campanaActualizada.getImagen());
            Campana actualizada = campanaRepository.save(campana);
            return ResponseEntity.ok(actualizada);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

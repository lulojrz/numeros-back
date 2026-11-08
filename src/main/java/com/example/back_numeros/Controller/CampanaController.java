package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.CampanaRepository;
import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.model.Campana;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/campanas")
public class CampanaController {

    private static final int MAX_IMAGE_LENGTH = 7_000_000;

    @Autowired
    CampanaRepository campanaRepository;

    @GetMapping("/traer")
    public List<Campana> traerCampanas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size, 10);
        return campanaRepository.findAll(pageable).getContent();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarCampana(@RequestBody Campana campana) {
        if (campana == null) {
            return ResponseEntity.badRequest().body("La campaña no puede ser nula");
        }
        if (imagenDemasiadoGrande(campana)) {
            return ResponseEntity.status(413).body("La imagen supera el tamaño permitido.");
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
        if (campanaActualizada == null) {
            return ResponseEntity.badRequest().body("La campaña no puede ser nula");
        }
        if (imagenDemasiadoGrande(campanaActualizada)) {
            return ResponseEntity.status(413).body("La imagen supera el tamaño permitido.");
        }
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

    private boolean imagenDemasiadoGrande(Campana campana) {
        return campana.getImagen() != null && campana.getImagen().length() > MAX_IMAGE_LENGTH;
    }
}

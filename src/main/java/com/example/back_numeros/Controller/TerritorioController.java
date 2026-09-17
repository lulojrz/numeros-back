package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.model.Territorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/territorios")
public class TerritorioController {

    @Autowired
    TerritorioRepository territorioRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE territorios MODIFY COLUMN imagen LONGTEXT");
        } catch (Exception e) {
            System.out.println("Nota: " + e.getMessage());
        }
    }

    @GetMapping("/traer")
    public List<Territorio> traerTerritorios() {
        return territorioRepository.findAll();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarTerritorio(@RequestBody Territorio territorio) {
        if (territorio == null) {
            return ResponseEntity.badRequest().body("El territorio no puede ser nulo");
        }
        Territorio guardado = territorioRepository.save(territorio);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarTerritorio(@PathVariable Long id) {
        Optional<Territorio> territorio = territorioRepository.findById(id);
        if (territorio.isPresent()) {
            territorioRepository.deleteById(id);
            return ResponseEntity.ok(territorio.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarTerritorio(@PathVariable Long id, @RequestBody Territorio territorioActualizado) {
        return territorioRepository.findById(id).map(territorio -> {
            territorio.setNumero(territorioActualizado.getNumero());
            territorio.setImagen(territorioActualizado.getImagen());
            territorio.setAsignadoA(territorioActualizado.getAsignadoA());
            territorio.setFechasTrabajado(territorioActualizado.getFechasTrabajado());
            // No tocamos la lista de manzanas aquí para evitar sobreescribirla por accidente
            Territorio actualizado = territorioRepository.save(territorio);
            return ResponseEntity.ok(actualizado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

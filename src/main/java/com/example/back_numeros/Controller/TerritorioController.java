package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.model.AsignacionTerritorio;
import java.time.LocalDateTime;
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
    AsignacionTerritorioRepository asignacionRepo;

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
            
            // Lógica de Historial de Asignaciones
            boolean assignedToNewUser = (territorioActualizado.getAsignadoA() != null && 
                                       (territorio.getAsignadoA() == null || 
                                       !territorio.getAsignadoA().getId().equals(territorioActualizado.getAsignadoA().getId())));
            
            boolean unassigned = (territorioActualizado.getAsignadoA() == null && territorio.getAsignadoA() != null);

            if (unassigned || assignedToNewUser) {
                // Cerrar asignación anterior si estaba abierta
                Optional<AsignacionTerritorio> openAsignacion = asignacionRepo.findFirstByTerritorioIdAndFechaDevolucionIsNullOrderByFechaAsignacionDesc(id);
                if (openAsignacion.isPresent()) {
                    AsignacionTerritorio asig = openAsignacion.get();
                    asig.setFechaDevolucion(LocalDateTime.now());
                    asignacionRepo.save(asig);
                }
            }

            if (assignedToNewUser) {
                // Crear nueva asignación
                AsignacionTerritorio nuevaAsig = new AsignacionTerritorio();
                nuevaAsig.setTerritorio(territorio);
                nuevaAsig.setUsuario(territorioActualizado.getAsignadoA());
                nuevaAsig.setFechaAsignacion(LocalDateTime.now());
                asignacionRepo.save(nuevaAsig);
            }

            territorio.setNumero(territorioActualizado.getNumero());
            territorio.setImagen(territorioActualizado.getImagen());
            territorio.setAsignadoA(territorioActualizado.getAsignadoA());
            territorio.setUltimaFechaTrabajada(territorioActualizado.getUltimaFechaTrabajada());
            territorio.setFechasTrabajado(territorioActualizado.getFechasTrabajado());
            
            Territorio actualizado = territorioRepository.save(territorio);
            return ResponseEntity.ok(actualizado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

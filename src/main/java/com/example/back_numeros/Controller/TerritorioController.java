package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.Repository.SalidaPredicacionRepository;
import com.example.back_numeros.Config.PaginationRequest;

import com.example.back_numeros.model.AsignacionTerritorio;
import com.example.back_numeros.model.SalidaPredicacion;
import java.time.LocalDateTime;
import com.example.back_numeros.model.Territorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/territorios")
public class TerritorioController {

    private static final int MAX_IMAGE_LENGTH = 7_000_000;

    @Autowired
    TerritorioRepository territorioRepository;

    @Autowired
    AsignacionTerritorioRepository asignacionRepo;

    @Autowired
    SalidaPredicacionRepository salidaRepo;

    @PostConstruct
    public void init() {
        registrarAsignacionesDeSalidasExistentes();
    }

    private void registrarAsignacionesDeSalidasExistentes() {
        int page = 0;
        Page<SalidaPredicacion> salidas;
        do {
            salidas = salidaRepo.findAll(Pageable.ofSize(100).withPage(page++));
            for (SalidaPredicacion salida : salidas.getContent()) {
                if (salida.getTerritorio() == null || salida.getConductor() == null ||
                        salida.getFecha() == null || salida.getHora() == null ||
                        asignacionRepo.existsBySalida_Id(salida.getId())) {
                    continue;
                }

                AsignacionTerritorio asignacion = new AsignacionTerritorio();
                asignacion.setTerritorio(salida.getTerritorio());
                asignacion.setUsuario(salida.getConductor());
                asignacion.setSalida(salida);
                asignacion.setFechaAsignacion(salida.getFecha().atTime(salida.getHora()));
                asignacion.setFechaDevolucion(salida.getFecha().atTime(LocalTime.of(23, 59, 59)));
                asignacionRepo.save(asignacion);
            }
        } while (salidas.hasNext());
    }

    @GetMapping("/traer")
    public List<Territorio> traerTerritorios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size, 10);
        return territorioRepository.findAll(pageable).getContent();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarTerritorio(@RequestBody Territorio territorio) {
        if (territorio == null) {
            return ResponseEntity.badRequest().body("El territorio no puede ser nulo");
        }
        if (imagenDemasiadoGrande(territorio)) {
            return ResponseEntity.status(413).body("La imagen supera el tamaño permitido.");
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> editarTerritorio(@PathVariable Long id, @RequestBody Territorio territorioActualizado) {
        if (territorioActualizado == null) {
            return ResponseEntity.badRequest().body("El territorio no puede ser nulo");
        }
        if (imagenDemasiadoGrande(territorioActualizado)) {
            return ResponseEntity.status(413).body("La imagen supera el tamaño permitido.");
        }
        Optional<Territorio> territorioEncontrado = territorioRepository.findByIdForUpdate(id);
        if (territorioEncontrado.isEmpty()) return ResponseEntity.notFound().build();

        Territorio territorio = territorioEncontrado.get();
        Long asignadoActualId = territorio.getAsignadoA() == null ? null : territorio.getAsignadoA().getId();
        Long nuevoAsignadoId = territorioActualizado.getAsignadoA() == null ? null : territorioActualizado.getAsignadoA().getId();
        boolean assignedToNewUser = nuevoAsignadoId != null && !nuevoAsignadoId.equals(asignadoActualId);
        boolean unassigned = nuevoAsignadoId == null && asignadoActualId != null;

        Optional<AsignacionTerritorio>  asignacionAbierta = Optional.empty();
        if (unassigned || assignedToNewUser) {
            asignacionAbierta = asignacionRepo.findFirstByTerritorioIdAndFechaDevolucionIsNullOrderByFechaAsignacionDesc(id);
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (assignedToNewUser) {
            Long asignacionAnteriorId = asignacionAbierta.map(AsignacionTerritorio::getId).orElse(null);
            long conflictos = asignacionRepo.countConflictingAssignmentsFrom(
                    id, nuevoAsignadoId, ahora, asignacionAnteriorId);
            if (conflictos > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El territorio está asignado a otro usuario durante ese período.");
            }
        }

        if (unassigned || assignedToNewUser) {
            asignacionAbierta.ifPresent(asignacion -> {
                asignacion.setFechaDevolucion(ahora);
                asignacionRepo.save(asignacion);
            });
        }

        if (assignedToNewUser) {
            AsignacionTerritorio nuevaAsignacion = new AsignacionTerritorio();
            nuevaAsignacion.setTerritorio(territorio);
            nuevaAsignacion.setUsuario(territorioActualizado.getAsignadoA());
            nuevaAsignacion.setFechaAsignacion(ahora);
            asignacionRepo.save(nuevaAsignacion);
        }

        territorio.setNumero(territorioActualizado.getNumero());
        territorio.setImagen(territorioActualizado.getImagen());
        territorio.setAsignadoA(territorioActualizado.getAsignadoA());
        territorio.setUltimaFechaTrabajada(territorioActualizado.getUltimaFechaTrabajada());
        territorio.setFechasTrabajado(territorioActualizado.getFechasTrabajado());

        return ResponseEntity.ok(territorioRepository.save(territorio));
    }

    private boolean imagenDemasiadoGrande(Territorio territorio) {
        return territorio.getImagen() != null && territorio.getImagen().length() > MAX_IMAGE_LENGTH;
    }

    @GetMapping("/asignaciones")
    public List<AsignacionTerritorio> traerAsignaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return asignacionRepo.findAll(pageable).getContent();
    }
}

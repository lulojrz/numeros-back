package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.Repository.SalidaPredicacionRepository;
import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.model.AsignacionTerritorio;
import com.example.back_numeros.model.SalidaPredicacion;
import com.example.back_numeros.model.Territorio;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/salidas")
public class SalidaPredicacionController {

    @Autowired
    SalidaPredicacionRepository salidaRepository;

    @Autowired
    AsignacionTerritorioRepository asignacionRepository;

    @Autowired
    TerritorioRepository territorioRepository;

    @GetMapping("/traer")
    public List<SalidaPredicacion> traerSalidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return salidaRepository.findAll(pageable).getContent();
    }

    @PostMapping("/agregar")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> agregarSalida(@RequestBody SalidaPredicacion salida) {
        if (salida == null || salida.getFecha() == null || salida.getHora() == null || salida.getConductor() == null) {
            return ResponseEntity.badRequest().build();
        }

        Territorio territorio = null;
        if (salida.getTerritorio() != null) {
            Long territorioId = salida.getTerritorio().getId();
            if (territorioId == null || salida.getConductor().getId() == null) {
                return ResponseEntity.badRequest().body("El territorio y el conductor deben ser válidos.");
            }

            Optional<Territorio> territorioEncontrado = territorioRepository.findByIdForUpdate(territorioId);
            if (territorioEncontrado.isEmpty()) return ResponseEntity.notFound().build();
            territorio = territorioEncontrado.get();

            LocalDateTime inicio = salida.getFecha().atTime(salida.getHora());
            LocalDateTime fin = salida.getFecha().atTime(LocalTime.MAX);
            if (tieneConflicto(territorio, territorioId, salida.getConductor().getId(), inicio, fin, null)) {
                return conflictoTerritorio();
            }
            salida.setTerritorio(territorio);
        }

        SalidaPredicacion salidaGuardada = salidaRepository.save(salida);
        registrarAsignacionTerritorio(salidaGuardada, territorio);
        return ResponseEntity.ok(salidaGuardada);
    }

    @PutMapping("/editar/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> editarSalida(@PathVariable Long id, @RequestBody SalidaPredicacion salidaActualizada) {
        if (salidaActualizada == null || salidaActualizada.getFecha() == null ||
                salidaActualizada.getHora() == null || salidaActualizada.getConductor() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<SalidaPredicacion> salidaEncontrada = salidaRepository.findById(id);
        if (salidaEncontrada.isEmpty()) return ResponseEntity.notFound().build();

        SalidaPredicacion salida = salidaEncontrada.get();
        Optional<AsignacionTerritorio> asignacionAnterior = asignacionRepository.findBySalida_Id(id);
        Territorio territorio = null;

        if (salidaActualizada.getTerritorio() != null) {
            Long territorioId = salidaActualizada.getTerritorio().getId();
            if (territorioId == null || salidaActualizada.getConductor().getId() == null) {
                return ResponseEntity.badRequest().body("El territorio y el conductor deben ser válidos.");
            }

            Optional<Territorio> territorioEncontrado = territorioRepository.findByIdForUpdate(territorioId);
            if (territorioEncontrado.isEmpty()) return ResponseEntity.notFound().build();
            territorio = territorioEncontrado.get();

            LocalDateTime inicio = salidaActualizada.getFecha().atTime(salidaActualizada.getHora());
            LocalDateTime fin = salidaActualizada.getFecha().atTime(LocalTime.MAX);
            Long asignacionAnteriorId = asignacionAnterior.map(AsignacionTerritorio::getId).orElse(null);
            if (tieneConflicto(territorio, territorioId, salidaActualizada.getConductor().getId(), inicio, fin, asignacionAnteriorId)) {
                return conflictoTerritorio();
            }
        }

        salida.setFecha(salidaActualizada.getFecha());
        salida.setHora(salidaActualizada.getHora());
        salida.setPuntoEncuentro(salidaActualizada.getPuntoEncuentro());
        salida.setGrupos(salidaActualizada.getGrupos());
        salida.setConductor(salidaActualizada.getConductor());
        salida.setTerritorio(territorio);
        SalidaPredicacion salidaGuardada = salidaRepository.save(salida);

        asignacionAnterior.ifPresent(asignacionRepository::delete);
        registrarAsignacionTerritorio(salidaGuardada, territorio);
        return ResponseEntity.ok(salidaGuardada);
    }

    @DeleteMapping("/borrar/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> borrarSalida(@PathVariable Long id) {
        Optional<SalidaPredicacion> salida = salidaRepository.findById(id);
        if (salida.isEmpty()) return ResponseEntity.notFound().build();

        if (salida.get().getTerritorio() != null) {
            territorioRepository.findByIdForUpdate(salida.get().getTerritorio().getId());
        }
        asignacionRepository.findBySalida_Id(id).ifPresent(asignacionRepository::delete);
        salidaRepository.delete(salida.get());
        return ResponseEntity.ok().build();
    }

    private boolean tieneConflicto(
            Territorio territorio,
            Long territorioId,
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin,
            Long asignacionExcluidaId) {
        if (asignacionRepository.countConflictingAssignments(
                territorioId, usuarioId, inicio, fin, asignacionExcluidaId) > 0) {
            return true;
        }

        if (fin.isBefore(LocalDateTime.now()) || territorio.getAsignadoA() == null ||
                territorio.getAsignadoA().getId().equals(usuarioId)) {
            return false;
        }

        return asignacionRepository.findFirstByTerritorioIdAndFechaDevolucionIsNullOrderByFechaAsignacionDesc(territorioId)
                .filter(asignacion -> asignacion.getUsuario().getId().equals(territorio.getAsignadoA().getId()))
                .isEmpty();
    }

    private ResponseEntity<String> conflictoTerritorio() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("El territorio ya está asignado a otro usuario durante ese período.");
    }

    private void registrarAsignacionTerritorio(SalidaPredicacion salida, Territorio territorio) {
        if (territorio == null) return;

        AsignacionTerritorio asignacion = new AsignacionTerritorio();
        asignacion.setTerritorio(territorio);
        asignacion.setUsuario(salida.getConductor());
        asignacion.setSalida(salida);
        asignacion.setFechaAsignacion(salida.getFecha().atTime(salida.getHora()));
        asignacion.setFechaDevolucion(salida.getFecha().atTime(salida.getHora()));
        asignacionRepository.save(asignacion);
    }
}

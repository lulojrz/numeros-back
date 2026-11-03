package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.Repository.SalidaPredicacionRepository;
import com.example.back_numeros.model.AsignacionTerritorio;
import com.example.back_numeros.model.SalidaPredicacion;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

@RestController
@RequestMapping("/salidas")
public class SalidaPredicacionController {

    @Autowired
    SalidaPredicacionRepository salidaRepository;

    @Autowired
    AsignacionTerritorioRepository asignacionRepository;

    @GetMapping("/traer")
    public List<SalidaPredicacion> traerSalidas() {
        return salidaRepository.findAll();
    }

    @PostMapping("/agregar")
    @Transactional
    public ResponseEntity<?> agregarSalida(@RequestBody SalidaPredicacion salida) {
        if (salida == null || salida.getFecha() == null || salida.getHora() == null || salida.getConductor() == null) {
            return ResponseEntity.badRequest().build();
        }

        SalidaPredicacion salidaGuardada = salidaRepository.save(salida);
        registrarAsignacionTerritorio(salidaGuardada);
        return ResponseEntity.ok(salidaGuardada);
    }

    @PutMapping("/editar/{id}")
    @Transactional
    public ResponseEntity<?> editarSalida(@PathVariable Long id, @RequestBody SalidaPredicacion salidaActualizada) {
        if (salidaActualizada == null || salidaActualizada.getFecha() == null ||
                salidaActualizada.getHora() == null || salidaActualizada.getConductor() == null) {
            return ResponseEntity.badRequest().build();
        }

        return salidaRepository.findById(id).map(salida -> {
            salida.setFecha(salidaActualizada.getFecha());
            salida.setHora(salidaActualizada.getHora());
            salida.setPuntoEncuentro(salidaActualizada.getPuntoEncuentro());
            salida.setGrupos(salidaActualizada.getGrupos());
            salida.setConductor(salidaActualizada.getConductor());
            salida.setTerritorio(salidaActualizada.getTerritorio());
            SalidaPredicacion salidaGuardada = salidaRepository.save(salida);

            asignacionRepository.findBySalida_Id(id).ifPresent(asignacionRepository::delete);
            registrarAsignacionTerritorio(salidaGuardada);

            return ResponseEntity.ok(salidaGuardada);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/borrar/{id}")
    @Transactional
    public ResponseEntity<?> borrarSalida(@PathVariable Long id) {
        Optional<SalidaPredicacion> salida = salidaRepository.findById(id);
        if (salida.isEmpty()) return ResponseEntity.notFound().build();

        asignacionRepository.findBySalida_Id(id).ifPresent(asignacionRepository::delete);
        salidaRepository.delete(salida.get());
        return ResponseEntity.ok().build();
    }

    private void registrarAsignacionTerritorio(SalidaPredicacion salida) {
        if (salida.getTerritorio() == null) return;

        AsignacionTerritorio asignacion = new AsignacionTerritorio();
        asignacion.setTerritorio(salida.getTerritorio());
        asignacion.setUsuario(salida.getConductor());
        asignacion.setSalida(salida);
        asignacion.setFechaAsignacion(salida.getFecha().atTime(salida.getHora()));
        asignacion.setFechaDevolucion(salida.getFecha().atTime(LocalTime.of(23, 59, 59)));
        asignacionRepository.save(asignacion);
    }
}

package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.Repository.SalidaPredicacionRepository;
import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.AsignacionTerritorio;
import com.example.back_numeros.model.SalidaPredicacion;
import com.example.back_numeros.model.Territorio;
import com.example.back_numeros.model.Usuario;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/salidas")
public class SalidaPredicacionController {

    @Autowired
    SalidaPredicacionRepository salidaRepository;

    @Autowired
    AsignacionTerritorioRepository asignacionRepository;

    @Autowired
    TerritorioRepository territorioRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

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
        if (salida == null || salida.getFecha() == null || salida.getHora() == null
                || salida.getConductor() == null || salida.getConductor().getId() == null) {
            return ResponseEntity.badRequest().body("Datos de la salida o conductor inválidos.");
        }

        // 1. Cargar la entidad real del conductor desde la BD
        Usuario conductorReal = usuarioRepository.findById(salida.getConductor().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conductor no encontrado."));
        salida.setConductor(conductorReal);

        LocalDateTime inicio = salida.getFecha().atTime(salida.getHora());
        LocalDateTime fin = salida.getFecha().atTime(LocalTime.MAX);
        Set<Long> territoriosProcesados = new HashSet<>();

        // 2. Validar y asignar territorio 1, 2 y 3 si fueron enviados
        Territorio t1 = validarYPrepararTerritorio(salida.getTerritorio(), conductorReal, inicio, fin, null, territoriosProcesados, true);
        Territorio t2 = validarYPrepararTerritorio(salida.getTerritorio2(), conductorReal, inicio, fin, null, territoriosProcesados, true);
        Territorio t3 = validarYPrepararTerritorio(salida.getTerritorio3(), conductorReal, inicio, fin, null, territoriosProcesados, true);

        salida.setTerritorio(t1);
        salida.setTerritorio2(t2);
        salida.setTerritorio3(t3);

        SalidaPredicacion salidaGuardada = salidaRepository.save(salida);

        // 3. Registrar asignaciones en historial para cada territorio asignado
        registrarAsignacionTerritorio(salidaGuardada, t1);
        registrarAsignacionTerritorio(salidaGuardada, t2);
        registrarAsignacionTerritorio(salidaGuardada, t3);

        return ResponseEntity.ok(salidaGuardada);
    }

    @PutMapping("/editar/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> editarSalida(@PathVariable Long id, @RequestBody SalidaPredicacion salidaActualizada) {
        if (salidaActualizada == null || salidaActualizada.getFecha() == null ||
                salidaActualizada.getHora() == null || salidaActualizada.getConductor() == null
                || salidaActualizada.getConductor().getId() == null) {
            return ResponseEntity.badRequest().body("Datos de la salida o conductor inválidos.");
        }

        Optional<SalidaPredicacion> salidaEncontrada = salidaRepository.findById(id);
        if (salidaEncontrada.isEmpty()) return ResponseEntity.notFound().build();

        Usuario conductorReal = usuarioRepository.findById(salidaActualizada.getConductor().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conductor no encontrado."));

        SalidaPredicacion salida = salidaEncontrada.get();
        List<AsignacionTerritorio> asignacionesAnteriores = asignacionRepository.findAllBySalida_Id(id);
        Long asignacionAnteriorId = asignacionesAnteriores.isEmpty() ? null : asignacionesAnteriores.get(0).getId();

        LocalDateTime inicio = salidaActualizada.getFecha().atTime(salidaActualizada.getHora());
        LocalDateTime fin = salidaActualizada.getFecha().atTime(LocalTime.MAX);
        Set<Long> territoriosProcesados = new HashSet<>();

        Territorio t1 = validarYPrepararTerritorio(salidaActualizada.getTerritorio(), conductorReal, inicio, fin, asignacionAnteriorId, territoriosProcesados, false);
        Territorio t2 = validarYPrepararTerritorio(salidaActualizada.getTerritorio2(), conductorReal, inicio, fin, asignacionAnteriorId, territoriosProcesados, false);
        Territorio t3 = validarYPrepararTerritorio(salidaActualizada.getTerritorio3(), conductorReal, inicio, fin, asignacionAnteriorId, territoriosProcesados, false);

        salida.setFecha(salidaActualizada.getFecha());
        salida.setHora(salidaActualizada.getHora());
        salida.setPuntoEncuentro(salidaActualizada.getPuntoEncuentro());
        salida.setGrupos(salidaActualizada.getGrupos());
        salida.setConductor(conductorReal);
        salida.setTerritorio(t1);
        salida.setTerritorio2(t2);
        salida.setTerritorio3(t3);
        SalidaPredicacion salidaGuardada = salidaRepository.save(salida);

        asignacionRepository.deleteAll(asignacionesAnteriores);
        registrarAsignacionTerritorio(salidaGuardada, t1);
        registrarAsignacionTerritorio(salidaGuardada, t2);
        registrarAsignacionTerritorio(salidaGuardada, t3);

        return ResponseEntity.ok(salidaGuardada);
    }

    @DeleteMapping("/borrar/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> borrarSalida(@PathVariable Long id) {
        Optional<SalidaPredicacion> salida = salidaRepository.findById(id);
        if (salida.isEmpty()) return ResponseEntity.notFound().build();

        SalidaPredicacion s = salida.get();
        if (s.getTerritorio() != null) territorioRepository.findByIdForUpdate(s.getTerritorio().getId());
        if (s.getTerritorio2() != null) territorioRepository.findByIdForUpdate(s.getTerritorio2().getId());
        if (s.getTerritorio3() != null) territorioRepository.findByIdForUpdate(s.getTerritorio3().getId());

        asignacionRepository.deleteAll(asignacionRepository.findAllBySalida_Id(id));
        salidaRepository.delete(s);
        return ResponseEntity.ok().build();
    }

    private Territorio validarYPrepararTerritorio(
            Territorio territorioRef,
            Usuario conductorReal,
            LocalDateTime inicio,
            LocalDateTime fin,
            Long asignacionExcluidaId,
            Set<Long> yaProcesados,
            boolean actualizarAsignadoA) {

        if (territorioRef == null || territorioRef.getId() == null) {
            return null;
        }

        Long territorioId = territorioRef.getId();
        // Si el usuario seleccionó el mismo territorio en dos desplegables, ignoramos el duplicado
        if (!yaProcesados.add(territorioId)) {
            return null;
        }

        Territorio territorio = territorioRepository.findByIdForUpdate(territorioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Territorio no encontrado: " + territorioId));

        if (tieneConflicto(territorio, territorioId, conductorReal.getId(), inicio, fin, asignacionExcluidaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El territorio " + territorio.getNumero() + " ya está asignado a otro usuario durante ese período.");
        }

        if (actualizarAsignadoA) {
            territorio.setAsignadoA(conductorReal);
            territorio = territorioRepository.save(territorio);
        }

        return territorio;
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
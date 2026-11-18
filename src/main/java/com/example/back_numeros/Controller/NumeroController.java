package com.example.back_numeros.Controller;

import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.Repository.NumeroRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Numero;
import com.example.back_numeros.model.Usuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api")
public class NumeroController {

    @Autowired
    NumeroRepository numeroRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @GetMapping("/numeros")
    public List<Numero> traerNumeros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String territorio,
            @RequestParam(required = false) String edificio,
            @RequestParam(required = false) Boolean reservado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta) {

        Pageable base = PaginationRequest.bounded(page, size);
        Pageable pageable = PageRequest.of(
                base.getPageNumber(),
                base.getPageSize(),
                Sort.by("territorio").ascending().and(Sort.by("edificio").ascending())
        );

        Specification<Numero> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (direccion != null && !direccion.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("direccion")), "%" + direccion.trim().toLowerCase() + "%"));
            }
            if (territorio != null && !territorio.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("territorio")), territorio.trim().toLowerCase()));
            }
            if (edificio != null && !edificio.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("edificio")), edificio.trim().toLowerCase()));
            }
            if (reservado != null) {
                predicates.add(cb.equal(root.get("reservado"), reservado));
            }
            if (fechaDesde != null && !fechaDesde.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ultimaFecha"), LocalDate.parse(fechaDesde).atStartOfDay()));
            }
            if (fechaHasta != null && !fechaHasta.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ultimaFecha"), LocalDate.parse(fechaHasta).atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return numeroRepository.findAll(spec, pageable).getContent();
    }

    @PutMapping("/numeros/reservar-lote")
    public ResponseEntity<?> reservarLote(@RequestBody Map<String, Object> payload) {
        List<?> rawIds = (List<?>) payload.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("actualizados", 0));
        }

        List<Long> ids = rawIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        String username = payload.get("usuario") != null ? payload.get("usuario").toString() : null;
        Usuario usuario = (username != null && !username.isBlank())
                ? usuarioRepository.findByUsuario(username).orElse(null)
                : null;

        int actualizados = numeroRepository.reservarLote(ids, LocalDateTime.now(), usuario);
        return ResponseEntity.ok(Map.of("actualizados", actualizados));
    }

    @PutMapping("/numeros/liberar-lote")
    public ResponseEntity<?> liberarLote(@RequestBody Map<String, Object> payload) {
        List<?> rawIds = (List<?>) payload.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("actualizados", 0));
        }

        List<Long> ids = rawIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        int actualizados = numeroRepository.liberarLote(ids);
        return ResponseEntity.ok(Map.of("actualizados", actualizados));
    }

    @PutMapping("/numeros/reiniciar-contesta-lote")
    public ResponseEntity<?> reiniciarContestaLote(@RequestBody Map<String, Object> payload) {
        List<?> rawIds = (List<?>) payload.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("actualizados", 0));
        }

        List<Long> ids = rawIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        int actualizados = numeroRepository.reiniciarContestaLote(ids);
        return ResponseEntity.ok(Map.of("actualizados", actualizados));
    }

    @PutMapping("/editar/{id}")
    public Numero editarNumero(@RequestBody Numero numero_a_editar, @PathVariable Long id) {
        return numeroRepository.findById(id).map(NumeroExistente -> {
            NumeroExistente.setContesta(numero_a_editar.getContesta());
            NumeroExistente.setReservado(numero_a_editar.getReservado());
            NumeroExistente.setUltimaFecha(numero_a_editar.getUltimaFecha());
            NumeroExistente.setTocar(numero_a_editar.getTocar());
            NumeroExistente.setEdificio(numero_a_editar.getEdificio());
            NumeroExistente.setFechaReserva(numero_a_editar.getFechaReserva());

            if (numero_a_editar.getUltUsuario() != null && numero_a_editar.getUltUsuario().getUsuario() != null) {
                Optional<Usuario> userCompleto = usuarioRepository.findByUsuario(numero_a_editar.getUltUsuario().getUsuario());
                userCompleto.ifPresent(NumeroExistente::setUltUsuario);
            } else {
                NumeroExistente.setUltUsuario(null);
            }

            if (numero_a_editar.getReservadoA() != null && numero_a_editar.getReservadoA().getUsuario() != null) {
                Optional<Usuario> userCompleto = usuarioRepository.findByUsuario(numero_a_editar.getReservadoA().getUsuario());
                userCompleto.ifPresent(NumeroExistente::setReservadoA);
            } else {
                NumeroExistente.setReservadoA(null);
            }

            return numeroRepository.save(NumeroExistente);
        }).orElse(null);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarNumero(@PathVariable Long id) {
        Optional<Numero> numero = numeroRepository.findById(id);
        if (numero.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        numeroRepository.delete(numero.get());
        return ResponseEntity.ok("Eliminado exitosamente");
    }

    @PostMapping("/agregar")
    public Numero agregarNumero(@RequestBody Numero numero) {
        if (numero != null) {
            if (numero.getUltimaFecha() == null) {
                numero.setUltimaFecha(LocalDateTime.now());
            }
            numeroRepository.save(numero);
        }
        return numero;
    }
}
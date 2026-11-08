package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.ReporteCarritoRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.model.ReporteCarrito;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/reporte")
public class ReporteCarritoController {
    @Autowired
    private ReporteCarritoRepository reporteCarritoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/subir")
    public ResponseEntity<?> subirReporte(
            @RequestBody ReporteCarrito reporteCarrito,
            Authentication authentication) {
        if (reporteCarrito == null) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }
        Optional<Usuario> usuario = usuarioRepository.findByUsuario(authentication.getName());
        if (usuario.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario autenticado no encontrado");
        }

        reporteCarrito.setUsuario(usuario.get());
        reporteCarrito.setFecha(java.time.LocalDateTime.now());
        reporteCarritoRepository.save(reporteCarrito);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/traer")
    public ResponseEntity<?> traerReportes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return ResponseEntity.ok(reporteCarritoRepository.findAll(pageable).getContent());
    }

    @DeleteMapping("eliminar/{id}")
    public ResponseEntity<?> eliminarReporte(@PathVariable Long id) {
        Optional<ReporteCarrito> reporte = reporteCarritoRepository.findById(id);
        if (reporte.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        reporteCarritoRepository.delete(reporte.get());
        return ResponseEntity.ok("Eliminado exitosamente");
    }
}

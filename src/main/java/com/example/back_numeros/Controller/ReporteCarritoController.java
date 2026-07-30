package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.ReporteCarritoRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.ReporteCarrito;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/reporte")
public class ReporteCarritoController {
    @Autowired
    private ReporteCarritoRepository reporteCarritoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/subir")
    public ResponseEntity<?> subirReporte(@RequestBody ReporteCarrito reporteCarrito){ 
        try {
            if (reporteCarrito.getUsuario() == null || reporteCarrito.getUsuario().getUsuario() == null) {
                return ResponseEntity.badRequest().body("Falta usuario en la peticion");
            }
            Optional<Usuario> u = usuarioRepository.findByUsuario(reporteCarrito.getUsuario().getUsuario());
            if (u.isPresent()) {
                reporteCarrito.setUsuario(u.get());
            } else {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }
            
            // Establecer la fecha en el servidor
            reporteCarrito.setFecha(java.time.LocalDateTime.now());
            
            reporteCarritoRepository.save(reporteCarrito);
            return ResponseEntity.ok().build(); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @GetMapping("/traer")
    public ResponseEntity<?> traerReportes() {
        return ResponseEntity.ok(reporteCarritoRepository.findAll());
    }
}

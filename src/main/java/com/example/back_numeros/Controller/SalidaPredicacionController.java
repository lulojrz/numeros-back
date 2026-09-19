package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.SalidaPredicacionRepository;
import com.example.back_numeros.model.SalidaPredicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/salidas")
public class SalidaPredicacionController {

    @Autowired
    SalidaPredicacionRepository salidaRepository;

    @GetMapping("/traer")
    public List<SalidaPredicacion> traerSalidas() {
        return salidaRepository.findAll();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarSalida(@RequestBody SalidaPredicacion salida) {
        if (salida == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(salidaRepository.save(salida));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarSalida(@PathVariable Long id, @RequestBody SalidaPredicacion salidaActualizada) {
        return salidaRepository.findById(id).map(salida -> {
            salida.setFecha(salidaActualizada.getFecha());
            salida.setHora(salidaActualizada.getHora());
            salida.setPuntoEncuentro(salidaActualizada.getPuntoEncuentro());
            salida.setConductor(salidaActualizada.getConductor());
            salida.setTerritorio(salidaActualizada.getTerritorio());
            return ResponseEntity.ok(salidaRepository.save(salida));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarSalida(@PathVariable Long id) {
        if (salidaRepository.existsById(id)) {
            salidaRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

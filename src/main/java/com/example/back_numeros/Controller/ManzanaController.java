package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.ManzanaRepository;
import com.example.back_numeros.model.Manzana;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/manzanas")
public class ManzanaController {

    @Autowired
    ManzanaRepository manzanaRepository;

    @GetMapping("/traer")
    public List<Manzana> traerManzanas() {
        return manzanaRepository.findAll();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarManzana(@RequestBody Manzana manzana) {
        if (manzana == null) {
            return ResponseEntity.badRequest().body("La manzana no puede ser nula");
        }
        Manzana guardada = manzanaRepository.save(manzana);
        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarManzana(@PathVariable Long id) {
        Optional<Manzana> manzana = manzanaRepository.findById(id);
        if (manzana.isPresent()) {
            manzanaRepository.deleteById(id);
            return ResponseEntity.ok(manzana.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarManzana(@PathVariable Long id, @RequestBody Manzana manzanaActualizada) {
        return manzanaRepository.findById(id).map(manzana -> {
            manzana.setNombre(manzanaActualizada.getNombre());
            manzana.setCalleNorte(manzanaActualizada.getCalleNorte());
            manzana.setCalleSur(manzanaActualizada.getCalleSur());
            manzana.setCalleEste(manzanaActualizada.getCalleEste());
            manzana.setCalleOeste(manzanaActualizada.getCalleOeste());
            manzana.setUltimaFechaTrabajada(manzanaActualizada.getUltimaFechaTrabajada());
            manzana.setCompletada(manzanaActualizada.getCompletada());
            if (manzanaActualizada.getTerritorio() != null) {
                manzana.setTerritorio(manzanaActualizada.getTerritorio());
            }
            Manzana actualizada = manzanaRepository.save(manzana);
            return ResponseEntity.ok(actualizada);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

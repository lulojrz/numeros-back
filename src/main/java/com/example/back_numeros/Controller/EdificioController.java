package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.EdificioRepository;
import com.example.back_numeros.model.Edificio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/edificios")
public class EdificioController {

    @Autowired
    EdificioRepository edificioRepository;

    @GetMapping("/traer")
    public List<Edificio> traerEdificios() {
        return edificioRepository.findAll();
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarEdificio(@RequestBody Edificio edificio) {
        if (edificio == null) {
            return ResponseEntity.badRequest().body("El edificio no puede ser nulo");
        }
        // Enlazar departamentos al edificio para que Hibernate asigne la llave foránea
        if (edificio.getDepartamentos() != null) {
            edificio.getDepartamentos().forEach(d -> d.setEdificio(edificio));
        }
        Edificio guardado = edificioRepository.save(edificio);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarEdificio(@PathVariable Long id) {
        Optional<Edificio> edificio = edificioRepository.findById(id);
        if (edificio.isPresent()) {
            edificioRepository.deleteById(id);
            return ResponseEntity.ok(edificio.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarEdificio(@PathVariable Long id, @RequestBody Edificio edificioActualizado) {
        return edificioRepository.findById(id).map(edificio -> {
            edificio.setDireccion(edificioActualizado.getDireccion());
            edificio.setCategoria(edificioActualizado.getCategoria());
            edificio.setCantidadPisos(edificioActualizado.getCantidadPisos());
            edificio.setDepartamentosPorPiso(edificioActualizado.getDepartamentosPorPiso());
            if (edificioActualizado.getManzana() != null) {
                edificio.setManzana(edificioActualizado.getManzana());
            }
            Edificio actualizado = edificioRepository.save(edificio);
            return ResponseEntity.ok(actualizado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

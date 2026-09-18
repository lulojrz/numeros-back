package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.DepartamentoRepository;
import com.example.back_numeros.model.Departamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    @Autowired
    DepartamentoRepository departamentoRepository;

    @GetMapping("/traer")
    public List<Departamento> traerDepartamentos() {
        return departamentoRepository.findAll();
    }

    @GetMapping("/revisitas/{publicadorId}")
    public List<Departamento> traerMisRevisitas(@PathVariable Long publicadorId) {
        return departamentoRepository.findByPublicadorIdAndEstado(publicadorId, "Revisita");
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarDepartamento(@RequestBody Departamento departamento) {
        if (departamento == null) {
            return ResponseEntity.badRequest().body("El departamento no puede ser nulo");
        }
        Departamento guardado = departamentoRepository.save(departamento);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarDepartamento(@PathVariable Long id) {
        Optional<Departamento> departamento = departamentoRepository.findById(id);
        if (departamento.isPresent()) {
            departamentoRepository.deleteById(id);
            return ResponseEntity.ok(departamento.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarDepartamento(@PathVariable Long id, @RequestBody Departamento departamentoActualizado) {
        return departamentoRepository.findById(id).map(departamento -> {
            departamento.setPiso(departamentoActualizado.getPiso());
            departamento.setLetra(departamentoActualizado.getLetra());
            departamento.setEstado(departamentoActualizado.getEstado());
            departamento.setTocar(departamentoActualizado.getTocar());
            departamento.setUltimaFechaTrabajada(departamentoActualizado.getUltimaFechaTrabajada());
            departamento.setObservaciones(departamentoActualizado.getObservaciones());
            departamento.setPublicador(departamentoActualizado.getPublicador());
            
            if (departamentoActualizado.getEdificio() != null) {
                departamento.setEdificio(departamentoActualizado.getEdificio());
            }
            
            Departamento actualizado = departamentoRepository.save(departamento);
            return ResponseEntity.ok(actualizado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

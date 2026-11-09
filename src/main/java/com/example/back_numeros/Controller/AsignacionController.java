package com.example.back_numeros.Controller;

import com.example.back_numeros.Config.PaginationRequest;
import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.model.AsignacionTerritorio;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AsignacionController {

    private final AsignacionTerritorioRepository asignacionRepo;

    public AsignacionController(AsignacionTerritorioRepository asignacionRepo) {
        this.asignacionRepo = asignacionRepo;
    }

    @GetMapping("/asignaciones")
    public List<AsignacionTerritorio> traerAsignaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return asignacionRepo.findAll(pageable).getContent();
    }
}

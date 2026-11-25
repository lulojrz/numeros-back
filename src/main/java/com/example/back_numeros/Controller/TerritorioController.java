package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Repository.AsignacionTerritorioRepository;
import com.example.back_numeros.Repository.SalidaPredicacionRepository;
import com.example.back_numeros.Config.PaginationRequest;

import com.example.back_numeros.Supabase.SupabaseStorageService;
import com.example.back_numeros.model.AsignacionTerritorio;
import com.example.back_numeros.model.SalidaPredicacion;

import java.io.IOException;
import java.time.LocalDateTime;
import com.example.back_numeros.model.Territorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/territorios")
public class TerritorioController {

    private static final int MAX_IMAGE_LENGTH = 7_000_000;

    private final SupabaseStorageService storageService;

    @Autowired
    TerritorioRepository territorioRepository;

    @Autowired
    AsignacionTerritorioRepository asignacionRepo;

    @Autowired
    SalidaPredicacionRepository salidaRepo;
    @Autowired
    private ObjectMapper objectMapper;

    public TerritorioController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    @PostConstruct
    public void init() {
        registrarAsignacionesDeSalidasExistentes();
    }

    private void registrarAsignacionesDeSalidasExistentes() {
        int page = 0;
        Page<SalidaPredicacion> salidas;
        do {
            salidas = salidaRepo.findAll(Pageable.ofSize(100).withPage(page++));
            for (SalidaPredicacion salida : salidas.getContent()) {
                if (salida.getTerritorio() == null || salida.getConductor() == null ||
                        salida.getFecha() == null || salida.getHora() == null ||
                        asignacionRepo.existsBySalida_Id(salida.getId())) {
                    continue;
                }

                AsignacionTerritorio asignacion = new AsignacionTerritorio();
                asignacion.setTerritorio(salida.getTerritorio());
                asignacion.setUsuario(salida.getConductor());
                asignacion.setSalida(salida);
                asignacion.setFechaAsignacion(salida.getFecha().atTime(salida.getHora()));
                asignacion.setFechaDevolucion((salida.getFecha().atTime(salida.getHora())));
                asignacionRepo.save(asignacion);
            }
        } while (salidas.hasNext());
    }

    @GetMapping("/traer")
    public List<Territorio> traerTerritorios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size, 100);
        return territorioRepository.findAll(pageable).getContent();
    }



    @PostMapping(value = "/agregar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> agregarTerritorio(
            @RequestParam("territorio") String territorioJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        try {
            if (territorioJson == null || territorioJson.isBlank()) {
                return ResponseEntity.badRequest().body("El territorio no puede ser nulo");
            }

            Territorio territorio = objectMapper.readValue(territorioJson, Territorio.class);

            if (imagen != null && !imagen.isEmpty()) {
                String imagenUrl = storageService.subirImagen(imagen, territorio.getNumero());
                territorio.setImagen(imagenUrl);
            }

            Territorio guardado = territorioRepository.save(territorio);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el territorio: " + e.getMessage());
        }
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarTerritorio(@PathVariable Long id) {
        Optional<Territorio> territorio = territorioRepository.findById(id);
        if (territorio.isPresent()) {
            territorioRepository.deleteById(id);
            return ResponseEntity.ok(territorio.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/editar/{id}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<?> editarTerritorio(@PathVariable Long id, @RequestBody Territorio territorioActualizado) {
        if (territorioActualizado == null) {
            return ResponseEntity.badRequest().body("La edicion no puede ser vacia");
        }
        if (imagenDemasiadoGrande(territorioActualizado)) {
            return ResponseEntity.status(413).body("La imagen supera el tamaño permitido.");
        }
        Optional<Territorio> territorioEncontrado = territorioRepository.findByIdForUpdate(id);
        if (territorioEncontrado.isEmpty()) return ResponseEntity.notFound().build();

        Territorio territorio = territorioEncontrado.get();


        territorio.setAsignadoA(territorioActualizado.getAsignadoA());
        territorio.setUltimaFechaTrabajada(territorioActualizado.getUltimaFechaTrabajada());
        territorio.setFechasTrabajado(territorioActualizado.getFechasTrabajado());

        return ResponseEntity.ok(territorioRepository.save(territorio));
    }

    private boolean imagenDemasiadoGrande(Territorio territorio) {
        return territorio.getImagen() != null && territorio.getImagen().length() > MAX_IMAGE_LENGTH;
    }

    @GetMapping("/asignaciones")
    public List<AsignacionTerritorio> traerAsignaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PaginationRequest.bounded(page, size);
        return asignacionRepo.findAll(pageable).getContent();
    }
}

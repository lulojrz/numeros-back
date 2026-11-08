package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.NumeroRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Numero;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import com.example.back_numeros.Config.PaginationRequest;

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
            @RequestParam(defaultValue = "50") int size) {
    Pageable pageable = PaginationRequest.bounded(page, size);
    return numeroRepository.findAll(pageable).getContent();
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


            // 1. Manejo de ultUsuario
            if (numero_a_editar.getUltUsuario() != null && numero_a_editar.getUltUsuario().getUsuario() != null) {
                Optional<Usuario> userCompleto = usuarioRepository.findByUsuario(numero_a_editar.getUltUsuario().getUsuario());
                userCompleto.ifPresent(NumeroExistente::setUltUsuario);
            } else {
                NumeroExistente.setUltUsuario(null);
            }

            // 2. Manejo de reservadoA (Corregido && y el else)
            if (numero_a_editar.getReservadoA() != null && numero_a_editar.getReservadoA().getUsuario() != null) {
                Optional<Usuario> userCompleto = usuarioRepository.findByUsuario(numero_a_editar.getReservadoA().getUsuario());
                userCompleto.ifPresent(NumeroExistente::setReservadoA);
            } else {
                NumeroExistente.setReservadoA(null); // <-- Ahora sí limpia su propio campo
            }



            return numeroRepository.save(NumeroExistente);
        }).orElse(null);
    }

@DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarNumero(@PathVariable Long id){
    Optional<Numero> numero = numeroRepository.findById(id);
    if (numero.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    numeroRepository.delete(numero.get());
    return ResponseEntity.ok("Eliminado exitosamente");
}

@PostMapping("/agregar")
    public Numero agregarNumero(@RequestBody Numero numero){
    if(numero != null){
         numeroRepository.save(numero);
    }
    return numero;
}



}

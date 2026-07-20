package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Turno;
import com.example.back_numeros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    // Acá podemos buscar turnos de una semana específica
    List<Turno> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate inicio, LocalDate fin);
    // Buscar turnos donde el usuario esté como publicador 1 O como publicador 2 (pasando el String del nombre)
    List<Turno> findByPublicador1_UsuarioOrPublicador2_Usuario(String usuario1, String usuario2);
    // En TurnoRepository.java
    List<Turno> findByFechaStartingWith(String year);
}
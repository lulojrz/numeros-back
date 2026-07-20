package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Turno;
import com.example.back_numeros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    // Acá podemos buscar turnos de una semana específica
    List<Turno> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate inicio, LocalDate fin);
    //buscar turnos por usuario
    List<Turno> findByUsuario(String usuario);
}
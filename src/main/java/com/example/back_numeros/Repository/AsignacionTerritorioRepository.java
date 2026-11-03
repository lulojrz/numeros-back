package com.example.back_numeros.Repository;

import com.example.back_numeros.model.AsignacionTerritorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface AsignacionTerritorioRepository extends JpaRepository<AsignacionTerritorio, Long> {
    List<AsignacionTerritorio> findByTerritorioIdOrderByFechaAsignacionAsc(Long territorioId);
    Optional<AsignacionTerritorio> findFirstByTerritorioIdAndFechaDevolucionIsNullOrderByFechaAsignacionDesc(Long territorioId);
    Optional<AsignacionTerritorio> findBySalida_Id(Long salidaId);
    boolean existsBySalida_Id(Long salidaId);

    @Query("""
            SELECT COUNT(a) FROM AsignacionTerritorio a
            WHERE a.territorio.id = :territorioId
              AND a.usuario.id <> :usuarioId
              AND a.fechaAsignacion <= :fechaFin
              AND (a.fechaDevolucion IS NULL OR a.fechaDevolucion >= :fechaInicio)
              AND (:asignacionExcluidaId IS NULL OR a.id <> :asignacionExcluidaId)
            """)
    long countConflictingAssignments(
            @Param("territorioId") Long territorioId,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("asignacionExcluidaId") Long asignacionExcluidaId);

    @Query("""
            SELECT COUNT(a) FROM AsignacionTerritorio a
            WHERE a.territorio.id = :territorioId
              AND a.usuario.id <> :usuarioId
              AND (a.fechaDevolucion IS NULL OR a.fechaDevolucion >= :fechaInicio)
              AND (:asignacionExcluidaId IS NULL OR a.id <> :asignacionExcluidaId)
            """)
    long countConflictingAssignmentsFrom(
            @Param("territorioId") Long territorioId,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("asignacionExcluidaId") Long asignacionExcluidaId);
}

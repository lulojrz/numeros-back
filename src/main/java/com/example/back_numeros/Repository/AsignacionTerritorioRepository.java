package com.example.back_numeros.Repository;

import com.example.back_numeros.model.AsignacionTerritorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionTerritorioRepository extends JpaRepository<AsignacionTerritorio, Long> {
    List<AsignacionTerritorio> findByTerritorioIdOrderByFechaAsignacionAsc(Long territorioId);
    Optional<AsignacionTerritorio> findFirstByTerritorioIdAndFechaDevolucionIsNullOrderByFechaAsignacionDesc(Long territorioId);
}

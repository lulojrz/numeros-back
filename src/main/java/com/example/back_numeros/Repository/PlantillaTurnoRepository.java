package com.example.back_numeros.Repository;

import com.example.back_numeros.model.PlantillaTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantillaTurnoRepository extends JpaRepository<PlantillaTurno, Long> {
}
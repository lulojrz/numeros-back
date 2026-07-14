package com.example.back_numeros.Repository;

import com.example.back_numeros.model.PuntoPredicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntoPredicacionRepository extends JpaRepository<PuntoPredicacion, Long> {
}
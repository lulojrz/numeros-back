package com.example.back_numeros.Repository;

import com.example.back_numeros.model.SalidaPredicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalidaPredicacionRepository extends JpaRepository<SalidaPredicacion, Long> {
}

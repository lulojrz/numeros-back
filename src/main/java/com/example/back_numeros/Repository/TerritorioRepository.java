package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Territorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerritorioRepository extends JpaRepository<Territorio, Long> {
}

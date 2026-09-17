package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Manzana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManzanaRepository extends JpaRepository<Manzana, Long> {
}

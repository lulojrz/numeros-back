package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Numero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumeroRepository extends JpaRepository<Numero,Long> {
}

package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Territorio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerritorioRepository extends JpaRepository<Territorio, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Territorio t WHERE t.id = :id")
    Optional<Territorio> findByIdForUpdate(@Param("id") Long id);
}

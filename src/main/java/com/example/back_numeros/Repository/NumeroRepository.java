package com.example.back_numeros.Repository;

import com.example.back_numeros.model.Numero;
import com.example.back_numeros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NumeroRepository extends JpaRepository<Numero, Long>, JpaSpecificationExecutor<Numero> {

    @Modifying
    @Transactional
    @Query("UPDATE Numero n SET n.reservado = true, n.fechaReserva = :fechaReserva, n.reservadoA = :usuario WHERE n.id IN :ids")
    int reservarLote(@Param("ids") List<Long> ids,
                     @Param("fechaReserva") LocalDateTime fechaReserva,
                     @Param("usuario") Usuario usuario);

    @Modifying
    @Transactional
    @Query("UPDATE Numero n SET n.reservado = false, n.fechaReserva = null, n.reservadoA = null WHERE n.id IN :ids")
    int liberarLote(@Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("UPDATE Numero n SET n.contesta = false WHERE n.id IN :ids")
    int reiniciarContestaLote(@Param("ids") List<Long> ids);

    long countByContestaTrue();

    long countByReservadoTrue(); // opcional, por si también quieres mostrar cuántos están reservados
    @Query("SELECT DISTINCT n.territorio FROM Numero n WHERE n.territorio IS NOT NULL AND n.territorio <> '' ORDER BY n.territorio ASC")
    List<String> findDistinctTerritorios();

    @Query("SELECT DISTINCT n.edificio FROM Numero n WHERE n.edificio IS NOT NULL AND n.edificio <> '' ORDER BY n.edificio ASC")
    List<String> findDistinctEdificios();
    // 1. Trae todos los números reservados de un usuario específico (sin límite de 50)
    List<Numero> findByReservadoTrueAndReservadoA_UsuarioOrderByTerritorioAscEdificioAsc(String usuario);

    // 2. Resumen agrupado para la tabla de administradores (1 sola consulta SQL rápida)
    @Query("SELECT n.reservadoA.usuario AS usuario, n.territorio AS territorio, MIN(n.fechaReserva) AS fechaAsignacion " +
            "FROM Numero n WHERE n.reservado = true AND n.reservadoA IS NOT NULL " +
            "GROUP BY n.reservadoA.usuario, n.territorio")
    List<Object[]> findResumenReservasPorUsuario();
}
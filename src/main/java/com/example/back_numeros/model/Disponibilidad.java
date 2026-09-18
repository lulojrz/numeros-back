package com.example.back_numeros.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidades")
@Data
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana; // Ej: "Lunes", "Martes"

    @Column(nullable = false)
    private LocalTime hora;
}

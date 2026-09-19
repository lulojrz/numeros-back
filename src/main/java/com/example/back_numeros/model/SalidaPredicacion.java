package com.example.back_numeros.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "salidas_predicacion")
@Data
public class SalidaPredicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(name = "punto_encuentro", nullable = false)
    private String puntoEncuentro;

    @Column(name = "grupos")
    private String grupos;

    @ManyToOne
    @JoinColumn(name = "conductor_id", nullable = false)
    private Usuario conductor;

    @ManyToOne
    @JoinColumn(name = "territorio_id", nullable = true)
    private Territorio territorio;
}

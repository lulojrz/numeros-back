package com.example.back_numeros.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salidas_predicacion")
@Getter
@Setter
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

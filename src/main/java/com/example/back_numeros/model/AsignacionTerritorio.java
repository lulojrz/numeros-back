package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "asignaciones_territorio")
@JsonIgnoreProperties("asignaciones")
@Getter
@Setter
public class AsignacionTerritorio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "territorio_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Territorio territorio;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToOne
    @JoinColumn(name = "salida_id", unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private SalidaPredicacion salida;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;
}

package com.example.back_numeros.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiencias")
@Data
public class Experiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;
    @ManyToOne
    @JoinColumn(name = "usuario")
    private Usuario usuario;

    private String titulo;
    private String descripcion;
}

package com.example.back_numeros.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "campanas")
@Data
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    
    private String estado; // Ejemplo: "Próxima", "Activa", "Finalizada"
    
    @Column(columnDefinition = "LONGTEXT")
    private String imagen; // Guardaremos la imagen en Base64 o como URL
}

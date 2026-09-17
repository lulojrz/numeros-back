package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "manzanas")
@Data
public class Manzana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; 

    @Column(name = "calle_norte")
    private String calleNorte;
    
    @Column(name = "calle_sur")
    private String calleSur;
    
    @Column(name = "calle_este")
    private String calleEste;
    
    @Column(name = "calle_oeste")
    private String calleOeste;

    @Column(name = "ultima_fecha_trabajada")
    private LocalDateTime ultimaFechaTrabajada;
    
    private Boolean completada; 

    @ManyToOne
    @JoinColumn(name = "territorio_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Territorio territorio;
}

package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "manzanas")
@Getter
@Setter
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

    @OneToMany(mappedBy = "manzana", cascade = CascadeType.ALL)
    private java.util.List<Edificio> edificios;
}

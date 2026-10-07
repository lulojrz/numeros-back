package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "edificios")
@Getter
@Setter
public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String direccion;
    private String categoria; 

    private Integer cantidadPisos; 
    private Integer departamentosPorPiso;

    @ManyToOne
    @JoinColumn(name = "manzana_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Manzana manzana;

    @OneToMany(mappedBy = "edificio", cascade = CascadeType.ALL)
    private List<Departamento> departamentos;
}

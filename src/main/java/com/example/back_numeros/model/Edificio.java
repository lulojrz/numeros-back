package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "edificios")
@Data
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

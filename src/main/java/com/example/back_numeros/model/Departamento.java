package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "departamentos")
@Data
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String piso;
    private String letra;
    private String estado; 
    private Boolean tocar; 

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "publicador_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Usuario publicador;

    @Column(name = "publicador_id", insertable = false, updatable = false)
    private Long idPublicador;

    @Column(name = "ultima_fecha_trabajada")
    private LocalDateTime ultimaFechaTrabajada;

    @ManyToOne
    @JoinColumn(name = "edificio_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Edificio edificio;
}

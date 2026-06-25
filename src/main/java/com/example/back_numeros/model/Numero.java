package com.example.back_numeros.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "numeros_telefonicos")
@Data
public class Numero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean contesta;

    private String direccion;
    private String territorio;
    private String manzana;
    private String numero;


    @Column(name = "ultima_fecha", nullable = false, updatable = false)
    private LocalDateTime ultimaFecha;

    private Boolean reservado;

}

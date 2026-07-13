package com.example.back_numeros.model;


import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String edificio;
    private String numero;


    @Column(name = "ultima_fecha", nullable = false)

    private LocalDateTime ultimaFecha;

    private Boolean reservado;
    @ManyToOne // Muchos números pueden haber sido modificados por un mismo Usuario
    @JoinColumn(name = "ultUsuario") // El nombre exacto de la columna en MySQL
    private Usuario ultUsuario; // Reemplazá 'Usuario' por el nombre de tu clase de usuarios
    private Boolean tocar;

    @ManyToOne
    @JoinColumn(name = "reservadoA")
    private Usuario reservadoA;

}

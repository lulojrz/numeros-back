package com.example.back_numeros.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "numeros_telefonicos",
        indexes = {
                @Index(name = "idx_num_terr_edif", columnList = "territorio, edificio"),
                @Index(name = "idx_num_reservado", columnList = "reservado"),
                @Index(name = "idx_num_ultima_fecha", columnList = "ultima_fecha"),
                @Index(name = "idx_num_direccion", columnList = "direccion")
        }
)
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

    @ManyToOne
    @JoinColumn(name = "ultUsuario")
    private Usuario ultUsuario;

    private Boolean tocar;

    @ManyToOne
    @JoinColumn(name = "reservadoA")
    private Usuario reservadoA;

    private LocalDateTime fechaReserva;
}
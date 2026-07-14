package com.example.back_numeros.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turnos_carritos")
@Getter
@Setter
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "punto_id", nullable = false)
    private PuntoPredicacion punto;

    // Primer publicador anotado
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publicador1_id")
    @JsonIgnoreProperties({"contrasena", "privilegio", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario publicador1;

    // Compañero del turno
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publicador2_id")
    @JsonIgnoreProperties({"contrasena", "privilegio", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario publicador2;

}
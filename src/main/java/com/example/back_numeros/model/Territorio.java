package com.example.back_numeros.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "territorios")
@Getter
@Setter
public class Territorio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    
    @Column(columnDefinition = "LONGTEXT")
    private String imagen; 

    @ManyToOne
    @JoinColumn(name = "asignado_a_id")
    private Usuario asignadoA;

    @Column(name = "ultima_fecha_trabajada")
    private LocalDateTime ultimaFechaTrabajada;

    @ElementCollection
    @CollectionTable(name = "territorio_fechas", joinColumns = @JoinColumn(name = "territorio_id"))
    @Column(name = "fecha_trabajado")
    private List<LocalDateTime> fechasTrabajado;

    @OneToMany(mappedBy = "territorio", cascade = CascadeType.ALL)
    private List<Manzana> manzanas;

    @OneToMany(mappedBy = "territorio", cascade = CascadeType.ALL)
    private List<AsignacionTerritorio> asignaciones;
}

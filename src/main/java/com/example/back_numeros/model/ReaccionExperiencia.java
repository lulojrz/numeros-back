package com.example.back_numeros.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="reaccion_experiencia")
@Data
public class ReaccionExperiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_nombre")
    @JsonIgnoreProperties({"contrasena", "privilegio", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
    private Usuario usuario;

    // 👇 ¡ESTO ES LO QUE FALTABA! Relación con la Experiencia 👇
    @ManyToOne
    @JoinColumn(name = "experiencia_id")
    @JsonIgnoreProperties("reacciones_list") // Para evitar bucles infinitos al convertir a JSON
    private Experiencia experiencia;

    private String tipo;

}
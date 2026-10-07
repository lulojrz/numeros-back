package com.example.back_numeros.model;

import jakarta.persistence.*;

import org.springframework.security.crypto.bcrypt.BCrypt;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios_telefonicos")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nombre;
    private String apellido;
    private String usuario;
    
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String contrasena;
    
    private String privilegio;
    private String asignacion;
    private String grupo;
    private Boolean banner;
    private Boolean carrito;
    private String telefono;
    @Column(name = "\"habilitacionPublica\"")
    private Boolean habilitacionPublica;



}

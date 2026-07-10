package com.example.back_numeros.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Entity
@Table(name = "usuarios_telefonicos")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nombre;
    private String apellido;
    private String usuario;
    private String contrasena;
    private String privilegio;


}

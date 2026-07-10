package com.example.back_numeros.model;


import lombok.Data;

@Data
public class cambiarContrasenaDTO {
    private String usuario;
    private String contrasenaActual;
    private String nuevaContrasena;

}

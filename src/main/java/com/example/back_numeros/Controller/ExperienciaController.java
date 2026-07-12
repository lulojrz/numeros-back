package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.ExperienciaRepository;
import com.example.back_numeros.model.Experiencia;
import com.example.back_numeros.model.Numero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/experiencias")
@CrossOrigin(origins = "https://colegiales.netlify.app")
public class ExperienciaController {
    @Autowired
    ExperienciaRepository experienciaRepository;

    @GetMapping("/traer")
    public List<Experiencia> traerExperiencias(){
        return experienciaRepository.findAll();

    }
    @PostMapping("/agregar")
    public Experiencia agregarExperiencia(@RequestBody Experiencia experiencia){
        if(experiencia != null){
            experienciaRepository.save(experiencia);
        }
        return experiencia;
    }
}

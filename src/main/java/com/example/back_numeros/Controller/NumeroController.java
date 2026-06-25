package com.example.back_numeros.Controller;

import com.example.back_numeros.Repository.NumeroRepository;
import com.example.back_numeros.model.Numero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
@CrossOrigin(origins = "*")


public class NumeroController {

@Autowired
    NumeroRepository numeroRepository;

@GetMapping("/numeros")
    public List<Numero>  traerNumeros(){
    return numeroRepository.findAll();
}




}

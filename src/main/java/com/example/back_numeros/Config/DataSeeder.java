package com.example.back_numeros.Config;

import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            if (u.getAsignacion() != null && u.getAsignacion().toLowerCase().contains("soporte")) {
                if (!"ROLE_ANC".equals(u.getPrivilegio())) {
                    u.setPrivilegio("ROLE_ANC");
                    usuarioRepository.save(u);
                }
            }
        }
    }
}

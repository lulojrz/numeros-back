package com.example.back_numeros.Controller;


import com.example.back_numeros.Repository.PlantillaTurnoRepository;
import com.example.back_numeros.Repository.PuntoPredicacionRepository;
import com.example.back_numeros.Repository.TurnoRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import com.example.back_numeros.model.PlantillaTurno;
import com.example.back_numeros.model.PuntoPredicacion;
import com.example.back_numeros.model.Turno;
import com.example.back_numeros.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "https://colegiales.netlify.app") // Ajustá según tu config de CORS
public class TurnoController {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private PlantillaTurnoRepository plantillaTurnoRepository;
    @Autowired
    private PuntoPredicacionRepository puntoPredicacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Tu repo de usuarios existente

    @GetMapping("/puntos")
    public List<PuntoPredicacion> obtenerPuntos(){
        return puntoPredicacionRepository.findAll();
    }
    @PostMapping("/puntos")
    public PuntoPredicacion agregarPunto(@RequestBody PuntoPredicacion puntoPredicacion){
        puntoPredicacionRepository.save(puntoPredicacion);
        return puntoPredicacion;

    }

    @PutMapping("/puntos/{id}")
    public PuntoPredicacion editarPunto(@PathVariable Long id, @RequestBody PuntoPredicacion puntoPredicacion_a_editar) {
        Optional<PuntoPredicacion> puntoPredicacion1 = puntoPredicacionRepository.findById(id);

        return puntoPredicacion1.map(puntoPredicacion2 -> {

            puntoPredicacion2.setActivo(puntoPredicacion_a_editar.isActivo());



            return puntoPredicacionRepository.save(puntoPredicacion2);
        }).orElseThrow(() -> new RuntimeException("No se encontró el punto con ID: " + id));
    }




    @PostMapping("/crear")
    public PlantillaTurno crearTurno(@RequestBody PlantillaTurno plantillaTurno){
        plantillaTurnoRepository.save(plantillaTurno);
        return plantillaTurno;
    }

    @GetMapping("/obtener")
    public List<PlantillaTurno> obtenerTurnos(){return plantillaTurnoRepository.findAll();}
    @DeleteMapping("/eliminar/{id}")
    public Optional<PlantillaTurno> borrarTurno(@PathVariable Long id){
        Optional<PlantillaTurno> turno = plantillaTurnoRepository.findById(id);
        plantillaTurnoRepository.deleteById(id);
        return turno;

    }



    // 1. TRAER TURNOS DE UNA SEMANA
    @GetMapping("/semana")
    public ResponseEntity<List<Turno>> obtenerTurnosSemana(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        // Calculamos el lunes y el domingo de la semana de la fecha recibida
        LocalDate lunes = fecha.with(DayOfWeek.MONDAY);
        LocalDate domingo = fecha.with(DayOfWeek.SUNDAY);

        List<Turno> turnos = turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(lunes, domingo);
        return ResponseEntity.ok(turnos);
    }

    // 2. GENERAR TURNOS SEMANALES DESDE LA PLANTILLA (Solo Admin)
    @PostMapping("/generar")
    public ResponseEntity<String> generarTurnosSemana(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lunes) {

        if (lunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            return ResponseEntity.badRequest().body("La fecha enviada debe ser un Lunes.");
        }

        List<PlantillaTurno> plantillas = plantillaTurnoRepository.findAll();
        if (plantillas.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay horarios configurados en la plantilla.");
        }

        // Evitar generar duplicados para la misma semana
        LocalDate domingo = lunes.plusDays(6);
        List<Turno> existentes = turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(lunes, domingo);
        if (!existentes.isEmpty()) {
            return ResponseEntity.badRequest().body("Ya existen turnos generados para esta semana.");
        }

        // Generamos los turnos de la semana usando el molde de la plantilla
        for (PlantillaTurno plantilla : plantillas) {
            int diasSumar = plantilla.getDiaSemana().getValue() - DayOfWeek.MONDAY.getValue();
            LocalDate fechaTurno = lunes.plusDays(diasSumar);

            Turno nuevoTurno = new Turno();
            nuevoTurno.setFecha(fechaTurno);
            nuevoTurno.setHoraInicio(plantilla.getHoraInicio());
            nuevoTurno.setHoraFin(plantilla.getHoraFin());
            nuevoTurno.setPunto(plantilla.getPunto());

            turnoRepository.save(nuevoTurno);
        }

        return ResponseEntity.ok("Se generaron con éxito los turnos para la semana del " + lunes);
    }

    // 3. ANOTARSE / DESANOTARSE EN UN TURNO
    @PutMapping("/anotarse/{id}")
    public ResponseEntity<?> anotarseEnTurno(
            @PathVariable Long id,
            @RequestParam String usuario) {

        Optional<Turno> turnoOpt = turnoRepository.findById(id);
        if (turnoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(usuario);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario especificado no existe.");
        }

        Turno turno = turnoOpt.get();
        Usuario user = usuarioOpt.get();

        // LÓGICA DE ASIGNACIÓN:
        // Si el usuario ya está anotado como publicador 1, lo removemos (desanotarse)
        if (turno.getPublicador1() != null && turno.getPublicador1().getId().equals(user.getId())) {
            turno.setPublicador1(null);
        }
        // Si ya está anotado como publicador 2, lo removemos
        else if (turno.getPublicador2() != null && turno.getPublicador2().getId().equals(user.getId())) {
            turno.setPublicador2(null);
        }
        // Si el puesto 1 está libre, se anota ahí
        else if (turno.getPublicador1() == null) {
            turno.setPublicador1(user);
        }
        // Si el puesto 1 está ocupado pero el 2 está libre, se anota de compañero
        else if (turno.getPublicador2() == null) {
            turno.setPublicador2(user);
        }
        // Si ambos puestos están ocupados por otros hermanos
        else {
            return ResponseEntity.badRequest().body("Este turno ya está completo.");
        }

        Turno actualizado = turnoRepository.save(turno);
        return ResponseEntity.ok(actualizado);
    }
}
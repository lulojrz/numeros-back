package com.example.back_numeros.model;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AsignacionTerritorioJsonTest {

    @Test
    void serializesTerritorioIdWithoutSerializingTerritorio() throws Exception {
        Territorio territorio = new Territorio();
        territorio.setId(42L);
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombre("Ana");
        usuario.setApellido("Prueba");
        usuario.setTelefono("555-1234");
        usuario.setPrivilegio("ROLE_ANC");

        AsignacionTerritorio asignacion = new AsignacionTerritorio();
        asignacion.setTerritorio(territorio);
        asignacion.setUsuario(usuario);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(asignacion));

        assertEquals(42L, json.get("territorioId").asLong());
        assertFalse(json.has("territorio"));
        assertEquals("Ana", json.get("usuario").get("nombre").asString());
        assertFalse(json.get("usuario").has("telefono"));
        assertFalse(json.get("usuario").has("privilegio"));
    }
}

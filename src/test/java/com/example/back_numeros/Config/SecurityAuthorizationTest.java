package com.example.back_numeros.Config;

import com.example.back_numeros.model.Territorio;
import com.example.back_numeros.model.Usuario;
import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TerritorioRepository territorioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void territoryListIsPublic() throws Exception {
        mockMvc.perform(get("/territorios/traer"))
                .andExpect(status().isOk());
    }

    @Test
    void publicTerritorySerializationOmitsAssignedUserPrivateFields() throws Exception {
        String username = "public-" + java.util.UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setUsuario(username);
        usuario.setTelefono("555-1234");
        usuario.setPrivilegio("ROLE_ANC");
        usuarioRepository.save(usuario);
        Territorio territorio = new Territorio();
        territorio.setAsignadoA(usuario);
        territorioRepository.save(territorio);

        String json = mockMvc.perform(get("/territorios/traer"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(json.contains(username));
        assertFalse(json.contains("555-1234"));
        assertFalse(json.contains("ROLE_ANC"));
        assertFalse(json.contains("asignaciones"));
    }

    @Test
    void otherDataEndpointsStillRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/numeros"))
                .andExpect(status().isForbidden());
    }

    @Test
    void passwordChangeDoesNotRequireLoginButStillRequiresCsrf() throws Exception {
        mockMvc.perform(put("/usuarios/cambiar-contrasena")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"missing\",\"contrasenaActual\":\"old\",\"nuevaContrasena\":\"new\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRequiresCsrfAndInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session-marker", "present");

        mockMvc.perform(post("/usuarios/logout")
                        .session(session)
                        .with(user("regular").authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios/logout")
                        .session(session)
                        .with(user("regular").authorities(() -> "ROLE_USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
    }

    @Test
    void userWithoutAncCannotCreateUsersOrDeleteRecords() throws Exception {
        var user = user("regular").authorities(() -> "ROLE_USER");

        mockMvc.perform(post("/usuarios/crear")
                        .with(user)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"test\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/territorios/borrar/999999")
                        .with(user)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void ancCanCreateUsers() throws Exception {
        mockMvc.perform(post("/usuarios/crear")
                        .with(user("admin").authorities(() -> "ROLE_ANC"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"csrf-check\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios/crear")
                        .with(user("admin").authorities(() -> "ROLE_ANC"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin-created\",\"contrasena\":\"test-password\",\"privilegio\":\"ROLE_USER\"}"))
                .andExpect(status().isOk());
    }
}

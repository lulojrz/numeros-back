package com.example.back_numeros.Config;

import com.example.back_numeros.model.Territorio;
import com.example.back_numeros.model.Usuario;
import com.example.back_numeros.Repository.TerritorioRepository;
import com.example.back_numeros.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

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
    private FilterRegistrationBean<CorsFilter> corsFilterRegistration;

    @Autowired
    private SessionRepository<?> sessionRepository;

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
    void productionFrontendCanMakeLoginCorsPreflight() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/usuarios/login");
        request.addHeader("Origin", "https://colegiales.netlify.app");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "content-type,x-xsrf-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        corsFilterRegistration.getFilter().doFilter(request, response, (req, res) -> {
            throw new AssertionError("CORS preflight should end in the CORS filter");
        });

        org.junit.jupiter.api.Assertions.assertEquals(200, response.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(
                "https://colegiales.netlify.app",
                response.getHeader("Access-Control-Allow-Origin"));
        org.junit.jupiter.api.Assertions.assertEquals(
                "true",
                response.getHeader("Access-Control-Allow-Credentials"));
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
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/asignaciones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserEndpointValidatesTheSessionAndReturnsOnlyTheCurrentUser() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized());

        String username = "session-" + java.util.UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setUsuario(username);
        usuario.setPrivilegio("ROLE_PUB");
        usuario.setAsignacion("telefonica");
        usuarioRepository.save(usuario);

        String response = mockMvc.perform(get("/usuarios/me")
                        .with(user(username).authorities(() -> "ROLE_PUB")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains(username));
        assertTrue(response.contains("telefonica"));
    }

    @Test
    void jdbcSessionRepositoryPersistsSessions() {
        Session restoredSession = saveAndReload(sessionRepository);

        assertTrue(restoredSession != null);
        assertTrue("persistent-user".equals(restoredSession.getAttribute("principal")));
    }

    private static <S extends Session> S saveAndReload(SessionRepository<S> repository) {
        S session = repository.createSession();
        session.setAttribute("principal", "persistent-user");
        repository.save(session);
        return repository.findById(session.getId());
    }

    @Test
    void assignmentExportPathWorksForAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/asignaciones")
                        .with(user("regular").authorities(() -> "ROLE_USER")))
                .andExpect(status().isOk());
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
        String adminUsername = "admin-" + java.util.UUID.randomUUID();
        Usuario admin = new Usuario();
        admin.setUsuario(adminUsername);
        admin.setPrivilegio("ROLE_ANC");
        usuarioRepository.save(admin);

        mockMvc.perform(post("/usuarios/crear")
                        .with(user(adminUsername).authorities(() -> "ROLE_ANC"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"csrf-check\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios/crear")
                        .with(user(adminUsername).authorities(() -> "ROLE_ANC"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin-created\",\"contrasena\":\"test-password\",\"privilegio\":\"ROLE_USER\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void supportAssignmentGrantsAdministrativeActionsRegardlessOfRole() throws Exception {
        String supportUsername = "support-" + java.util.UUID.randomUUID();
        Usuario support = new Usuario();
        support.setUsuario(supportUsername);
        support.setPrivilegio("ROLE_PUB");
        support.setAsignacion("Soporte");
        usuarioRepository.save(support);

        mockMvc.perform(post("/usuarios/crear")
                        .with(user(supportUsername).authorities(() -> "ROLE_PUB"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"support-created\",\"contrasena\":\"test-password\",\"privilegio\":\"ROLE_USER\"}"))
                .andExpect(status().isOk());

        support.setAsignacion("");
        usuarioRepository.save(support);

        mockMvc.perform(post("/usuarios/crear")
                        .with(user(supportUsername).authorities(() -> "ROLE_PUB"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"support-revoked\",\"contrasena\":\"test-password\",\"privilegio\":\"ROLE_USER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editingUserPersistsProfileFieldsAndRejectsDuplicateUsernames() throws Exception {
        String adminUsername = "admin-edit-" + java.util.UUID.randomUUID();
        Usuario admin = new Usuario();
        admin.setUsuario(adminUsername);
        admin.setPrivilegio("ROLE_ANC");
        usuarioRepository.save(admin);

        Usuario target = new Usuario();
        target.setUsuario("target-" + java.util.UUID.randomUUID());
        target.setPrivilegio("ROLE_PUB");
        target.setGrupo("Grupo 1");
        target = usuarioRepository.save(target);

        mockMvc.perform(put("/usuarios/editar/" + target.getId())
                        .with(user(adminUsername).authorities(() -> "ROLE_ANC"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nombre actualizado\",\"apellido\":\"Apellido actualizado\",\"usuario\":\"cuenta-actualizada\",\"telefono\":\"5551234\",\"privilegio\":\"ROLE_PUB\",\"asignacion\":\"\",\"grupo\":\"Grupo 4\",\"carrito\":true,\"banner\":false,\"habilitacionPublica\":false,\"conductor\":false}"))
                .andExpect(status().isOk());

        Usuario updated = usuarioRepository.findById(target.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Nombre actualizado", updated.getNombre());
        org.junit.jupiter.api.Assertions.assertEquals("Apellido actualizado", updated.getApellido());
        org.junit.jupiter.api.Assertions.assertEquals("cuenta-actualizada", updated.getUsuario());
        org.junit.jupiter.api.Assertions.assertEquals("Grupo 4", updated.getGrupo());
        org.junit.jupiter.api.Assertions.assertTrue(updated.getCarrito());

        mockMvc.perform(put("/usuarios/editar/" + target.getId())
                        .with(user(adminUsername).authorities(() -> "ROLE_ANC"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"" + adminUsername + "\",\"privilegio\":\"ROLE_PUB\"}"))
                .andExpect(status().isConflict());
    }
}

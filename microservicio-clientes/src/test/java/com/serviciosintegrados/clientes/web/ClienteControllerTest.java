package com.serviciosintegrados.clientes.web;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.serviciosintegrados.clientes.dto.ClienteResponse;
import com.serviciosintegrados.clientes.error.ConflictoException;
import com.serviciosintegrados.clientes.error.GlobalExceptionHandler;
import com.serviciosintegrados.clientes.error.RecursoNoEncontradoException;
import com.serviciosintegrados.clientes.service.ClienteService;

class ClienteControllerTest {

    private static final String CUERPO_VALIDO = """
            {"nombre":"Ana Torres","documento":"1020304050","email":"ana@correo.com",
             "telefono":"3001234567","direccion":"Bogotá"}
            """;

    private ClienteService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(ClienteService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ClienteController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crear_devuelve201ConUbicacion() throws Exception {
        when(servicio.crear(any())).thenReturn(new ClienteResponse(
                1L, "Ana Torres", "1020304050", "ana@correo.com", "3001234567", "Bogotá", Instant.now()));

        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/clientes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana Torres"));
    }

    @Test
    void crear_conDatosInvalidos_devuelve400ConDetalleDeErrores() throws Exception {
        String cuerpoInvalido = """
                {"nombre":"","documento":"123","email":"no-es-correo"}
                """;

        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cuerpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.email").exists());
    }

    @Test
    void crear_conCorreoRepetido_devuelve409() throws Exception {
        when(servicio.crear(any())).thenThrow(new ConflictoException("Ya existe un cliente con el correo"));

        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe un cliente con el correo"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(servicio.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe el cliente con id 99"));

        mvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el cliente con id 99"));
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el cliente con id 5"))
                .when(servicio).eliminar(5L);

        mvc.perform(delete("/api/clientes/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void idNoNumerico_devuelve400() throws Exception {
        mvc.perform(get("/api/clientes/abc"))
                .andExpect(status().isBadRequest());
    }
}

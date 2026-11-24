package com.serviciosintegrados.ventas.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.serviciosintegrados.ventas.dto.ItemResponse;
import com.serviciosintegrados.ventas.dto.VentaResponse;
import com.serviciosintegrados.ventas.error.ConflictoException;
import com.serviciosintegrados.ventas.error.GlobalExceptionHandler;
import com.serviciosintegrados.ventas.error.RecursoNoEncontradoException;
import com.serviciosintegrados.ventas.error.ServicioNoDisponibleException;
import com.serviciosintegrados.ventas.model.EstadoVenta;
import com.serviciosintegrados.ventas.service.VentaService;

class VentaControllerTest {

    private static final String CUERPO_VALIDO = """
            {"clienteId":10,"items":[{"productoId":501,"cantidad":3}]}
            """;

    private VentaService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(VentaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new VentaController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registrar_devuelve201() throws Exception {
        when(servicio.registrar(any())).thenReturn(new VentaResponse(
                1L, "VTA-ABC12345", 10L, "Ana Torres", Instant.now(), EstadoVenta.REGISTRADA,
                new BigDecimal("300000.00"),
                List.of(new ItemResponse(501L, "Teclado", 3, new BigDecimal("100000.00"), new BigDecimal("300000.00")))));

        mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("REGISTRADA"))
                .andExpect(jsonPath("$.items[0].cantidad").value(3));
    }

    @Test
    void registrar_sinItems_devuelve400() throws Exception {
        mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.items").exists());
    }

    @Test
    void registrar_conCantidadInvalidaEnUnaLinea_devuelve400() throws Exception {
        mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"items\":[{\"productoId\":501,\"cantidad\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_conStockInsuficiente_devuelve409() throws Exception {
        when(servicio.registrar(any())).thenThrow(new ConflictoException("Stock insuficiente para el producto 501"));

        mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void registrar_conClienteInexistente_devuelve404() throws Exception {
        when(servicio.registrar(any())).thenThrow(new RecursoNoEncontradoException("El cliente 10 no existe"));

        mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrar_siUnaDependenciaFalla_devuelve503() throws Exception {
        when(servicio.registrar(any())).thenThrow(
                new ServicioNoDisponibleException("El servicio de inventario no está disponible", new RuntimeException("timeout")));

        mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("El servicio de inventario no está disponible"));
    }
}

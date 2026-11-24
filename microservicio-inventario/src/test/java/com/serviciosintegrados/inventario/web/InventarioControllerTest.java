package com.serviciosintegrados.inventario.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.serviciosintegrados.inventario.dto.InventarioResponse;
import com.serviciosintegrados.inventario.error.ConflictoException;
import com.serviciosintegrados.inventario.error.GlobalExceptionHandler;
import com.serviciosintegrados.inventario.service.InventarioService;

class InventarioControllerTest {

    private InventarioService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(InventarioService.class);
        mvc = MockMvcBuilders.standaloneSetup(new InventarioController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void entrada_devuelveElInventarioActualizado() throws Exception {
        when(servicio.registrarEntrada(eq(501L), any()))
                .thenReturn(new InventarioResponse(501L, 30, Instant.now()));

        mvc.perform(post("/api/inventario/501/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":30,\"referencia\":\"COMPRA-9\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(501))
                .andExpect(jsonPath("$.cantidad").value(30));
    }

    @Test
    void salida_conStockInsuficiente_devuelve409() throws Exception {
        when(servicio.registrarSalida(eq(501L), any()))
                .thenThrow(new ConflictoException("Stock insuficiente para el producto 501"));

        mvc.perform(post("/api/inventario/501/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":99}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Stock insuficiente para el producto 501"));
    }

    @Test
    void movimiento_conCantidadCero_devuelve400() throws Exception {
        mvc.perform(post("/api/inventario/501/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidad").exists());
    }

    @Test
    void movimiento_sinCantidad_devuelve400() throws Exception {
        mvc.perform(post("/api/inventario/501/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

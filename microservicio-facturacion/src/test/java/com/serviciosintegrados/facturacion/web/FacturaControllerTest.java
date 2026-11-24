package com.serviciosintegrados.facturacion.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.serviciosintegrados.facturacion.dto.FacturaResponse;
import com.serviciosintegrados.facturacion.error.ConflictoException;
import com.serviciosintegrados.facturacion.error.GlobalExceptionHandler;
import com.serviciosintegrados.facturacion.error.RecursoNoEncontradoException;
import com.serviciosintegrados.facturacion.error.ServicioNoDisponibleException;
import com.serviciosintegrados.facturacion.service.FacturaService;

class FacturaControllerTest {

    private FacturaService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(FacturaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new FacturaController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void generar_devuelve201() throws Exception {
        when(servicio.generar(8L)).thenReturn(new FacturaResponse(
                1L, "FAC-20261114-AB12CD34", 8L, 10L, "Ana Torres", Instant.now(),
                new BigDecimal("100000.00"), new BigDecimal("19000.00"), new BigDecimal("119000.00")));

        mvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content("{\"ventaId\":8}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("FAC-20261114-AB12CD34"))
                .andExpect(jsonPath("$.total").value(119000.00));
    }

    @Test
    void generar_sinVenta_devuelve400() throws Exception {
        mvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.ventaId").exists());
    }

    @Test
    void generar_ventaYaFacturada_devuelve409() throws Exception {
        when(servicio.generar(8L)).thenThrow(new ConflictoException("La venta 8 ya tiene una factura emitida"));

        mvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content("{\"ventaId\":8}"))
                .andExpect(status().isConflict());
    }

    @Test
    void generar_siVentasNoResponde_devuelve503() throws Exception {
        when(servicio.generar(8L)).thenThrow(
                new ServicioNoDisponibleException("El servicio de ventas no está disponible", new RuntimeException()));

        mvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content("{\"ventaId\":8}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void obtenerPorVenta_sinFactura_devuelve404() throws Exception {
        when(servicio.obtenerPorVenta(5L)).thenThrow(new RecursoNoEncontradoException("La venta 5 no tiene factura"));

        mvc.perform(get("/api/facturas/venta/5"))
                .andExpect(status().isNotFound());
    }
}

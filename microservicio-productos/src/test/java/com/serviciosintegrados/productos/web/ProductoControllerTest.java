package com.serviciosintegrados.productos.web;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.serviciosintegrados.productos.dto.ProductoResponse;
import com.serviciosintegrados.productos.error.GlobalExceptionHandler;
import com.serviciosintegrados.productos.error.RecursoNoEncontradoException;
import com.serviciosintegrados.productos.service.ProductoService;

class ProductoControllerTest {

    private ProductoService servicio;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        servicio = mock(ProductoService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ProductoController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crear_devuelve201() throws Exception {
        when(servicio.crear(any())).thenReturn(new ProductoResponse(
                10L, "LAP-001", "Portátil", "Equipo", new BigDecimal("2500000.00"), true));
        String cuerpo = """
                {"sku":"LAP-001","nombre":"Portátil","descripcion":"Equipo","precio":2500000.00}
                """;

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/productos/10")))
                .andExpect(jsonPath("$.sku").value("LAP-001"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crear_conPrecioNegativo_devuelve400() throws Exception {
        String cuerpo = """
                {"sku":"LAP-001","nombre":"Portátil","precio":-5}
                """;

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precio").exists());
    }

    @Test
    void crear_sinPrecio_devuelve400() throws Exception {
        String cuerpo = """
                {"sku":"LAP-001","nombre":"Portátil"}
                """;

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precio").value("El precio es obligatorio"));
    }

    @Test
    void listar_porDefectoNoFiltraPorActivos() throws Exception {
        when(servicio.listar(false)).thenReturn(List.of());

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(servicio).listar(false);
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(servicio.obtener(77L)).thenThrow(new RecursoNoEncontradoException("No existe el producto con id 77"));

        mvc.perform(get("/api/productos/77"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el producto con id 77"));
    }
}

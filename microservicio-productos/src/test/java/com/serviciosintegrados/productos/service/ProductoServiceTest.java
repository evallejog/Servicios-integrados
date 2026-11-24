package com.serviciosintegrados.productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.serviciosintegrados.productos.dto.ProductoRequest;
import com.serviciosintegrados.productos.dto.ProductoResponse;
import com.serviciosintegrados.productos.error.ConflictoException;
import com.serviciosintegrados.productos.error.RecursoNoEncontradoException;
import com.serviciosintegrados.productos.model.Producto;
import com.serviciosintegrados.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repositorio;

    @InjectMocks
    private ProductoService servicio;

    private static ProductoRequest solicitud(Boolean activo) {
        return new ProductoRequest(" lap-001 ", "  Portátil 14 pulgadas ", "Equipo de oficina",
                new BigDecimal("2500000.00"), activo);
    }

    @Test
    void crear_normalizaSkuYActivaPorDefecto() {
        when(repositorio.existsBySku("LAP-001")).thenReturn(false);
        when(repositorio.save(any(Producto.class))).thenAnswer(invocacion -> {
            Producto guardado = invocacion.getArgument(0);
            ReflectionTestUtils.setField(guardado, "id", 3L);
            return guardado;
        });

        ProductoResponse respuesta = servicio.crear(solicitud(null));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repositorio).save(captor.capture());
        assertThat(captor.getValue().getSku()).isEqualTo("LAP-001");
        assertThat(captor.getValue().getNombre()).isEqualTo("Portátil 14 pulgadas");
        assertThat(respuesta.id()).isEqualTo(3L);
        assertThat(respuesta.activo()).isTrue();
    }

    @Test
    void crear_conSkuDuplicado_lanzaConflicto() {
        when(repositorio.existsBySku("LAP-001")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(solicitud(true)))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("LAP-001");
        verify(repositorio, never()).save(any());
    }

    @Test
    void actualizar_sinIndicarActivo_conservaElEstadoActual() {
        Producto existente = new Producto();
        existente.setActivo(false);
        when(repositorio.findById(4L)).thenReturn(Optional.of(existente));
        when(repositorio.existsBySkuAndIdNot("LAP-001", 4L)).thenReturn(false);
        when(repositorio.save(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ProductoResponse respuesta = servicio.actualizar(4L, solicitud(null));

        assertThat(respuesta.activo()).isFalse();
        assertThat(respuesta.precio()).isEqualByComparingTo("2500000.00");
    }

    @Test
    void listar_soloActivos_usaLaConsultaFiltrada() {
        Producto activo = new Producto();
        activo.setSku("A-1");
        activo.setNombre("Activo");
        activo.setPrecio(BigDecimal.TEN);
        when(repositorio.findByActivoTrueOrderByNombre()).thenReturn(List.of(activo));

        List<ProductoResponse> productos = servicio.listar(true);

        assertThat(productos).hasSize(1);
        verify(repositorio, never()).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void obtener_inexistente_lanzaNoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminar_inexistente_lanzaNoEncontrado() {
        when(repositorio.existsById(8L)).thenReturn(false);

        assertThatThrownBy(() -> servicio.eliminar(8L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repositorio, never()).deleteById(any());
    }
}

package com.serviciosintegrados.facturacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.serviciosintegrados.facturacion.client.VentaRemota;
import com.serviciosintegrados.facturacion.client.VentasClient;
import com.serviciosintegrados.facturacion.dto.FacturaResponse;
import com.serviciosintegrados.facturacion.error.ConflictoException;
import com.serviciosintegrados.facturacion.error.RecursoNoEncontradoException;
import com.serviciosintegrados.facturacion.error.ServicioNoDisponibleException;
import com.serviciosintegrados.facturacion.model.Factura;
import com.serviciosintegrados.facturacion.repository.FacturaRepository;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturas;

    @Mock
    private VentasClient ventas;

    private FacturaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new FacturaService(facturas, ventas, new BigDecimal("19"));
    }

    private void simularGuardado() {
        when(facturas.save(any(Factura.class))).thenAnswer(invocacion -> {
            Factura factura = invocacion.getArgument(0);
            ReflectionTestUtils.setField(factura, "id", 1L);
            return factura;
        });
    }

    @Test
    void generar_calculaIvaYTotal() {
        when(facturas.existsByVentaId(8L)).thenReturn(false);
        when(ventas.obtener(8L)).thenReturn(
                new VentaRemota(8L, 10L, "Ana Torres", new BigDecimal("100000.00"), "REGISTRADA"));
        simularGuardado();

        FacturaResponse factura = servicio.generar(8L);

        assertThat(factura.subtotal()).isEqualByComparingTo("100000.00");
        assertThat(factura.iva()).isEqualByComparingTo("19000.00");
        assertThat(factura.total()).isEqualByComparingTo("119000.00");
        assertThat(factura.numero()).matches("FAC-\\d{8}-[0-9A-F]{8}");
        verify(ventas).marcarFacturada(8L);
    }

    @Test
    void generar_redondeaElIvaADosDecimales() {
        when(facturas.existsByVentaId(9L)).thenReturn(false);
        when(ventas.obtener(9L)).thenReturn(
                new VentaRemota(9L, 10L, "Ana Torres", new BigDecimal("333.33"), "REGISTRADA"));
        simularGuardado();

        FacturaResponse factura = servicio.generar(9L);

        assertThat(factura.iva()).isEqualByComparingTo("63.33");
        assertThat(factura.total()).isEqualByComparingTo("396.66");
    }

    @Test
    void generar_siLaVentaYaTieneFactura_lanzaConflicto() {
        when(facturas.existsByVentaId(8L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.generar(8L))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("8");
        verify(ventas, never()).obtener(any());
        verify(facturas, never()).save(any());
    }

    @Test
    void generar_conVentaInexistente_propagaNoEncontrado() {
        when(facturas.existsByVentaId(77L)).thenReturn(false);
        when(ventas.obtener(77L)).thenThrow(new RecursoNoEncontradoException("La venta 77 no existe"));

        assertThatThrownBy(() -> servicio.generar(77L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(facturas, never()).save(any());
    }

    @Test
    void generar_siNoSePuedeNotificarAVentas_laFacturaIgualSeEmite() {
        when(facturas.existsByVentaId(8L)).thenReturn(false);
        when(ventas.obtener(8L)).thenReturn(
                new VentaRemota(8L, 10L, "Ana Torres", new BigDecimal("100000.00"), "REGISTRADA"));
        simularGuardado();
        doThrow(new ServicioNoDisponibleException("ventas caído", new RuntimeException("timeout")))
                .when(ventas).marcarFacturada(8L);

        FacturaResponse factura = servicio.generar(8L);

        assertThat(factura.id()).isEqualTo(1L);
    }
}

package com.serviciosintegrados.ventas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.serviciosintegrados.ventas.client.ClienteRemoto;
import com.serviciosintegrados.ventas.client.ClientesClient;
import com.serviciosintegrados.ventas.client.InventarioClient;
import com.serviciosintegrados.ventas.client.ProductoRemoto;
import com.serviciosintegrados.ventas.client.ProductosClient;
import com.serviciosintegrados.ventas.dto.ItemRequest;
import com.serviciosintegrados.ventas.dto.VentaRequest;
import com.serviciosintegrados.ventas.dto.VentaResponse;
import com.serviciosintegrados.ventas.error.ConflictoException;
import com.serviciosintegrados.ventas.error.RecursoNoEncontradoException;
import com.serviciosintegrados.ventas.error.ReglaNegocioException;
import com.serviciosintegrados.ventas.model.EstadoVenta;
import com.serviciosintegrados.ventas.model.Venta;
import com.serviciosintegrados.ventas.repository.VentaRepository;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventas;

    @Mock
    private ClientesClient clientes;

    @Mock
    private ProductosClient productos;

    @Mock
    private InventarioClient inventario;

    @InjectMocks
    private VentaService servicio;

    private static ProductoRemoto producto(long id, String nombre, String precio, boolean activo) {
        return new ProductoRemoto(id, "SKU-" + id, nombre, new BigDecimal(precio), activo);
    }

    private void simularGuardado() {
        when(ventas.save(any(Venta.class))).thenAnswer(invocacion -> {
            Venta venta = invocacion.getArgument(0);
            ReflectionTestUtils.setField(venta, "id", 1L);
            return venta;
        });
    }

    @Test
    void registrar_calculaElTotalYDescuentaElInventario() {
        when(clientes.obtener(10L)).thenReturn(new ClienteRemoto(10L, "Ana Torres"));
        when(productos.obtener(501L)).thenReturn(producto(501, "Teclado", "100000.00", true));
        when(productos.obtener(502L)).thenReturn(producto(502, "Mouse", "50000.00", true));
        simularGuardado();

        VentaResponse respuesta = servicio.registrar(new VentaRequest(10L, List.of(
                new ItemRequest(501L, 3), new ItemRequest(502L, 2))));

        assertThat(respuesta.total()).isEqualByComparingTo("400000.00");
        assertThat(respuesta.estado()).isEqualTo(EstadoVenta.REGISTRADA);
        assertThat(respuesta.clienteNombre()).isEqualTo("Ana Torres");
        assertThat(respuesta.items()).hasSize(2);
        verify(inventario).registrarSalida(eq(501L), eq(3), startsWith("VTA-"));
        verify(inventario).registrarSalida(eq(502L), eq(2), startsWith("VTA-"));
    }

    @Test
    void registrar_consolidaLineasDelMismoProducto() {
        when(clientes.obtener(10L)).thenReturn(new ClienteRemoto(10L, "Ana Torres"));
        when(productos.obtener(501L)).thenReturn(producto(501, "Teclado", "100000.00", true));
        simularGuardado();

        VentaResponse respuesta = servicio.registrar(new VentaRequest(10L, List.of(
                new ItemRequest(501L, 1), new ItemRequest(501L, 2))));

        assertThat(respuesta.items()).hasSize(1);
        assertThat(respuesta.items().get(0).cantidad()).isEqualTo(3);
        verify(inventario).registrarSalida(eq(501L), eq(3), anyString());
    }

    @Test
    void registrar_siFallaUnaLinea_revierteLasUnidadesYaDescontadas() {
        when(clientes.obtener(10L)).thenReturn(new ClienteRemoto(10L, "Ana Torres"));
        when(productos.obtener(501L)).thenReturn(producto(501, "Teclado", "100000.00", true));
        when(productos.obtener(502L)).thenReturn(producto(502, "Mouse", "50000.00", true));
        doThrow(new ConflictoException("Stock insuficiente para el producto 502"))
                .when(inventario).registrarSalida(eq(502L), anyInt(), anyString());

        assertThatThrownBy(() -> servicio.registrar(new VentaRequest(10L, List.of(
                new ItemRequest(501L, 3), new ItemRequest(502L, 2)))))
                .isInstanceOf(ConflictoException.class);

        verify(inventario).registrarEntrada(eq(501L), eq(3), startsWith("REVERSA-VTA-"));
        verify(inventario, never()).registrarEntrada(eq(502L), anyInt(), anyString());
        verify(ventas, never()).save(any());
    }

    @Test
    void registrar_conClienteInexistente_noTocaInventario() {
        when(clientes.obtener(99L)).thenThrow(new RecursoNoEncontradoException("El cliente 99 no existe"));

        assertThatThrownBy(() -> servicio.registrar(new VentaRequest(99L, List.of(new ItemRequest(501L, 1)))))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(inventario, never()).registrarSalida(any(), anyInt(), anyString());
    }

    @Test
    void registrar_conProductoInactivo_lanzaReglaDeNegocio() {
        when(clientes.obtener(10L)).thenReturn(new ClienteRemoto(10L, "Ana Torres"));
        when(productos.obtener(501L)).thenReturn(producto(501, "Teclado", "100000.00", false));

        assertThatThrownBy(() -> servicio.registrar(new VentaRequest(10L, List.of(new ItemRequest(501L, 1)))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Teclado");

        verify(inventario, never()).registrarSalida(any(), anyInt(), anyString());
    }

    @Test
    void marcarFacturada_cambiaElEstado() {
        Venta venta = Venta.registrar("VTA-TEST", 10L, "Ana Torres", List.of());
        when(ventas.findById(1L)).thenReturn(java.util.Optional.of(venta));
        when(ventas.save(any(Venta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        VentaResponse respuesta = servicio.marcarFacturada(1L);

        assertThat(respuesta.estado()).isEqualTo(EstadoVenta.FACTURADA);
    }
}

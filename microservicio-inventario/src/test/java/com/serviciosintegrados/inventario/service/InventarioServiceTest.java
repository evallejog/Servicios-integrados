package com.serviciosintegrados.inventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.serviciosintegrados.inventario.dto.InventarioResponse;
import com.serviciosintegrados.inventario.dto.MovimientoRequest;
import com.serviciosintegrados.inventario.error.ConflictoException;
import com.serviciosintegrados.inventario.error.RecursoNoEncontradoException;
import com.serviciosintegrados.inventario.model.Inventario;
import com.serviciosintegrados.inventario.model.MovimientoInventario;
import com.serviciosintegrados.inventario.model.TipoMovimiento;
import com.serviciosintegrados.inventario.repository.InventarioRepository;
import com.serviciosintegrados.inventario.repository.MovimientoInventarioRepository;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    private static final Long PRODUCTO = 501L;

    @Mock
    private InventarioRepository inventarios;

    @Mock
    private MovimientoInventarioRepository movimientos;

    @InjectMocks
    private InventarioService servicio;

    @Test
    void entrada_sobreProductoSinRegistro_creaElInventario() {
        when(inventarios.buscarParaActualizar(PRODUCTO)).thenReturn(Optional.empty());
        when(inventarios.save(any(Inventario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        InventarioResponse respuesta = servicio.registrarEntrada(PRODUCTO, new MovimientoRequest(25, "COMPRA-1"));

        assertThat(respuesta.productoId()).isEqualTo(PRODUCTO);
        assertThat(respuesta.cantidad()).isEqualTo(25);
        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientos).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(captor.getValue().getReferencia()).isEqualTo("COMPRA-1");
    }

    @Test
    void entrada_sobreInventarioExistente_acumulaUnidades() {
        Inventario existente = new Inventario(PRODUCTO);
        existente.aumentar(10);
        when(inventarios.buscarParaActualizar(PRODUCTO)).thenReturn(Optional.of(existente));
        when(inventarios.save(any(Inventario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        InventarioResponse respuesta = servicio.registrarEntrada(PRODUCTO, new MovimientoRequest(5, null));

        assertThat(respuesta.cantidad()).isEqualTo(15);
    }

    @Test
    void salida_conExistencias_descuentaYRegistraElMovimiento() {
        Inventario existente = new Inventario(PRODUCTO);
        existente.aumentar(10);
        when(inventarios.buscarParaActualizar(PRODUCTO)).thenReturn(Optional.of(existente));
        when(inventarios.save(any(Inventario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        InventarioResponse respuesta = servicio.registrarSalida(PRODUCTO, new MovimientoRequest(4, "VENTA-1"));

        assertThat(respuesta.cantidad()).isEqualTo(6);
        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientos).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoMovimiento.SALIDA);
        assertThat(captor.getValue().getCantidad()).isEqualTo(4);
    }

    @Test
    void salida_conStockInsuficiente_lanzaConflictoYNoDescuenta() {
        Inventario existente = new Inventario(PRODUCTO);
        existente.aumentar(3);
        when(inventarios.buscarParaActualizar(PRODUCTO)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> servicio.registrarSalida(PRODUCTO, new MovimientoRequest(4, "VENTA-2")))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("disponible 3, solicitado 4");
        assertThat(existente.getCantidad()).isEqualTo(3);
        verify(inventarios, never()).save(any());
        verify(movimientos, never()).save(any());
    }

    @Test
    void salida_sobreProductoSinRegistro_lanzaConflicto() {
        when(inventarios.buscarParaActualizar(PRODUCTO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrarSalida(PRODUCTO, new MovimientoRequest(1, null)))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void obtener_sinRegistro_lanzaNoEncontrado() {
        when(inventarios.findByProductoId(PRODUCTO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(PRODUCTO))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}

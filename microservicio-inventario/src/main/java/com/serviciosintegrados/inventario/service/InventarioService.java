package com.serviciosintegrados.inventario.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serviciosintegrados.inventario.dto.InventarioResponse;
import com.serviciosintegrados.inventario.dto.MovimientoRequest;
import com.serviciosintegrados.inventario.dto.MovimientoResponse;
import com.serviciosintegrados.inventario.error.ConflictoException;
import com.serviciosintegrados.inventario.error.RecursoNoEncontradoException;
import com.serviciosintegrados.inventario.model.Inventario;
import com.serviciosintegrados.inventario.model.MovimientoInventario;
import com.serviciosintegrados.inventario.model.TipoMovimiento;
import com.serviciosintegrados.inventario.repository.InventarioRepository;
import com.serviciosintegrados.inventario.repository.MovimientoInventarioRepository;

@Service
public class InventarioService {

    private final InventarioRepository inventarios;
    private final MovimientoInventarioRepository movimientos;

    public InventarioService(InventarioRepository inventarios, MovimientoInventarioRepository movimientos) {
        this.inventarios = inventarios;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> listar() {
        return inventarios.findAll(Sort.by("productoId")).stream()
                .map(InventarioResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponse obtener(Long productoId) {
        return inventarios.findByProductoId(productoId)
                .map(InventarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay registro de inventario para el producto " + productoId));
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientos(Long productoId) {
        return movimientos.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(MovimientoResponse::desde)
                .toList();
    }

    @Transactional
    public InventarioResponse registrarEntrada(Long productoId, MovimientoRequest datos) {
        int unidades = datos.cantidad();
        Inventario inventario = inventarios.buscarParaActualizar(productoId)
                .orElseGet(() -> new Inventario(productoId));

        inventario.aumentar(unidades);
        Inventario guardado = inventarios.save(inventario);
        movimientos.save(new MovimientoInventario(productoId, TipoMovimiento.ENTRADA, unidades, datos.referencia()));
        return InventarioResponse.desde(guardado);
    }

    @Transactional
    public InventarioResponse registrarSalida(Long productoId, MovimientoRequest datos) {
        int unidades = datos.cantidad();
        Inventario inventario = inventarios.buscarParaActualizar(productoId)
                .orElseThrow(() -> new ConflictoException(
                        "Stock insuficiente para el producto " + productoId + ": no hay existencias registradas"));

        if (!inventario.hayExistencias(unidades)) {
            throw new ConflictoException("Stock insuficiente para el producto " + productoId
                    + ": disponible " + inventario.getCantidad() + ", solicitado " + unidades);
        }

        inventario.disminuir(unidades);
        Inventario guardado = inventarios.save(inventario);
        movimientos.save(new MovimientoInventario(productoId, TipoMovimiento.SALIDA, unidades, datos.referencia()));
        return InventarioResponse.desde(guardado);
    }
}

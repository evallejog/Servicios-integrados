package com.serviciosintegrados.inventario.dto;

import java.time.Instant;

import com.serviciosintegrados.inventario.model.MovimientoInventario;
import com.serviciosintegrados.inventario.model.TipoMovimiento;

public record MovimientoResponse(
        Long id,
        Long productoId,
        TipoMovimiento tipo,
        int cantidad,
        String referencia,
        Instant fecha) {

    public static MovimientoResponse desde(MovimientoInventario movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getProductoId(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getReferencia(),
                movimiento.getFecha());
    }
}

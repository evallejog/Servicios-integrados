package com.serviciosintegrados.inventario.dto;

import java.time.Instant;

import com.serviciosintegrados.inventario.model.Inventario;

public record InventarioResponse(Long productoId, int cantidad, Instant actualizadoEn) {

    public static InventarioResponse desde(Inventario inventario) {
        return new InventarioResponse(
                inventario.getProductoId(),
                inventario.getCantidad(),
                inventario.getActualizadoEn());
    }
}

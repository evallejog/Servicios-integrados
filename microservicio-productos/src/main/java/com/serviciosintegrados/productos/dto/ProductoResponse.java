package com.serviciosintegrados.productos.dto;

import java.math.BigDecimal;

import com.serviciosintegrados.productos.model.Producto;

public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        BigDecimal precio,
        boolean activo) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.isActivo());
    }
}

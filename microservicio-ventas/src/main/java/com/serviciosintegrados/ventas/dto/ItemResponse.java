package com.serviciosintegrados.ventas.dto;

import java.math.BigDecimal;

import com.serviciosintegrados.ventas.model.VentaItem;

public record ItemResponse(
        Long productoId,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {

    public static ItemResponse desde(VentaItem item) {
        return new ItemResponse(
                item.getProductoId(),
                item.getNombreProducto(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getSubtotal());
    }
}

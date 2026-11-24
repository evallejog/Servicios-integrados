package com.serviciosintegrados.ventas.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.serviciosintegrados.ventas.model.EstadoVenta;
import com.serviciosintegrados.ventas.model.Venta;

public record VentaResponse(
        Long id,
        String referencia,
        Long clienteId,
        String clienteNombre,
        Instant fecha,
        EstadoVenta estado,
        BigDecimal total,
        List<ItemResponse> items) {

    public static VentaResponse desde(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getReferencia(),
                venta.getClienteId(),
                venta.getClienteNombre(),
                venta.getFecha(),
                venta.getEstado(),
                venta.getTotal(),
                venta.getItems().stream().map(ItemResponse::desde).toList());
    }
}

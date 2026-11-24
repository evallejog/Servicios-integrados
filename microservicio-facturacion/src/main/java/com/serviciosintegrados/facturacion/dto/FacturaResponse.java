package com.serviciosintegrados.facturacion.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.serviciosintegrados.facturacion.model.Factura;

public record FacturaResponse(
        Long id,
        String numero,
        Long ventaId,
        Long clienteId,
        String clienteNombre,
        Instant fechaEmision,
        BigDecimal subtotal,
        BigDecimal iva,
        BigDecimal total) {

    public static FacturaResponse desde(Factura factura) {
        return new FacturaResponse(
                factura.getId(),
                factura.getNumero(),
                factura.getVentaId(),
                factura.getClienteId(),
                factura.getClienteNombre(),
                factura.getFechaEmision(),
                factura.getSubtotal(),
                factura.getIva(),
                factura.getTotal());
    }
}

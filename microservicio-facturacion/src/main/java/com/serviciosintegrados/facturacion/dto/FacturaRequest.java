package com.serviciosintegrados.facturacion.dto;

import jakarta.validation.constraints.NotNull;

public record FacturaRequest(
        @NotNull(message = "La venta es obligatoria")
        Long ventaId) {
}

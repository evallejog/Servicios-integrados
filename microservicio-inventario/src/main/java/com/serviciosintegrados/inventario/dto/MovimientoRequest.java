package com.serviciosintegrados.inventario.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 1_000_000, message = "La cantidad no puede superar 1.000.000 de unidades")
        Integer cantidad,

        @Size(max = 80, message = "La referencia no puede superar los 80 caracteres")
        String referencia) {
}

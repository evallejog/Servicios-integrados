package com.serviciosintegrados.ventas.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VentaRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        @NotEmpty(message = "La venta debe incluir al menos un producto")
        @Size(max = 50, message = "Una venta admite máximo 50 líneas")
        List<@Valid ItemRequest> items) {
}

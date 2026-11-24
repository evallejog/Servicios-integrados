package com.serviciosintegrados.facturacion.client;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VentaRemota(Long id, Long clienteId, String clienteNombre, BigDecimal total, String estado) {
}

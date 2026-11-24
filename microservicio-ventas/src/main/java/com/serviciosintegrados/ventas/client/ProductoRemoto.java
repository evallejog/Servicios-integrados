package com.serviciosintegrados.ventas.client;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoRemoto(Long id, String sku, String nombre, BigDecimal precio, boolean activo) {
}

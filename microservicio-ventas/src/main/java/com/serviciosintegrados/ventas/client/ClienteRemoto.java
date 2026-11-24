package com.serviciosintegrados.ventas.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteRemoto(Long id, String nombre) {
}

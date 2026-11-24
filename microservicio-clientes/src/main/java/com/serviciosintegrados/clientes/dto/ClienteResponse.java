package com.serviciosintegrados.clientes.dto;

import java.time.Instant;

import com.serviciosintegrados.clientes.model.Cliente;

public record ClienteResponse(
        Long id,
        String nombre,
        String documento,
        String email,
        String telefono,
        String direccion,
        Instant creadoEn) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getCreadoEn());
    }
}

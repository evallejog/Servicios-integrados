package com.serviciosintegrados.inventario.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serviciosintegrados.inventario.dto.InventarioResponse;
import com.serviciosintegrados.inventario.dto.MovimientoRequest;
import com.serviciosintegrados.inventario.dto.MovimientoResponse;
import com.serviciosintegrados.inventario.service.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService servicio;

    public InventarioController(InventarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<InventarioResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{productoId}")
    public InventarioResponse obtener(@PathVariable Long productoId) {
        return servicio.obtener(productoId);
    }

    @GetMapping("/{productoId}/movimientos")
    public List<MovimientoResponse> movimientos(@PathVariable Long productoId) {
        return servicio.listarMovimientos(productoId);
    }

    @PostMapping("/{productoId}/entrada")
    public InventarioResponse entrada(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest datos) {
        return servicio.registrarEntrada(productoId, datos);
    }

    @PostMapping("/{productoId}/salida")
    public InventarioResponse salida(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest datos) {
        return servicio.registrarSalida(productoId, datos);
    }
}

package com.serviciosintegrados.facturacion.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.serviciosintegrados.facturacion.dto.FacturaRequest;
import com.serviciosintegrados.facturacion.dto.FacturaResponse;
import com.serviciosintegrados.facturacion.service.FacturaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService servicio;

    public FacturaController(FacturaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<FacturaResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public FacturaResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/venta/{ventaId}")
    public FacturaResponse obtenerPorVenta(@PathVariable Long ventaId) {
        return servicio.obtenerPorVenta(ventaId);
    }

    @PostMapping
    public ResponseEntity<FacturaResponse> generar(@Valid @RequestBody FacturaRequest solicitud) {
        FacturaResponse emitida = servicio.generar(solicitud.ventaId());
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(emitida.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(emitida);
    }
}

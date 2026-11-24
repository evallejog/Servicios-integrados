package com.serviciosintegrados.facturacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serviciosintegrados.facturacion.client.VentaRemota;
import com.serviciosintegrados.facturacion.client.VentasClient;
import com.serviciosintegrados.facturacion.dto.FacturaResponse;
import com.serviciosintegrados.facturacion.error.ConflictoException;
import com.serviciosintegrados.facturacion.error.RecursoNoEncontradoException;
import com.serviciosintegrados.facturacion.model.Factura;
import com.serviciosintegrados.facturacion.repository.FacturaRepository;

@Service
public class FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final FacturaRepository facturas;
    private final VentasClient ventas;
    private final BigDecimal ivaPorcentaje;

    public FacturaService(FacturaRepository facturas, VentasClient ventas,
                          @Value("${facturacion.iva-porcentaje}") BigDecimal ivaPorcentaje) {
        this.facturas = facturas;
        this.ventas = ventas;
        this.ivaPorcentaje = ivaPorcentaje;
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listar() {
        return facturas.findAllByOrderByFechaEmisionDesc().stream().map(FacturaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtener(Long id) {
        return FacturaResponse.desde(facturas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con id " + id)));
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtenerPorVenta(Long ventaId) {
        return FacturaResponse.desde(facturas.findByVentaId(ventaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La venta " + ventaId + " no tiene factura")));
    }

    /**
     * Emite la factura de una venta. El subtotal es el total de la venta (precios sin IVA) y el IVA
     * se calcula con el porcentaje configurado.
     */
    public FacturaResponse generar(Long ventaId) {
        if (facturas.existsByVentaId(ventaId)) {
            throw new ConflictoException("La venta " + ventaId + " ya tiene una factura emitida");
        }

        VentaRemota venta = ventas.obtener(ventaId);
        BigDecimal subtotal = venta.total();
        BigDecimal iva = subtotal.multiply(ivaPorcentaje).divide(CIEN, 2, RoundingMode.HALF_UP);

        Factura factura = new Factura(generarNumero(), venta.id(), venta.clienteId(), venta.clienteNombre(),
                subtotal, iva);
        FacturaResponse emitida = FacturaResponse.desde(facturas.save(factura));

        notificarVenta(ventaId);
        return emitida;
    }

    /** Avisar a Ventas es un efecto secundario: si falla, la factura ya emitida no se pierde. */
    private void notificarVenta(Long ventaId) {
        try {
            ventas.marcarFacturada(ventaId);
        } catch (RuntimeException ex) {
            log.warn("La factura de la venta {} se emitió, pero no se pudo actualizar su estado: {}",
                    ventaId, ex.getMessage());
        }
    }

    private static String generarNumero() {
        String fecha = LocalDate.now(ZoneOffset.UTC).format(FORMATO_FECHA);
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FAC-" + fecha + "-" + sufijo;
    }
}

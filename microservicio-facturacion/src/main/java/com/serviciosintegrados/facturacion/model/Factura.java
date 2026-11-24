package com.serviciosintegrados.facturacion.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Factura emitida a partir de una venta. La restricción de unicidad sobre {@code ventaId}
 * garantiza que una venta no pueda facturarse dos veces, incluso con solicitudes concurrentes.
 */
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Column(nullable = false, unique = true)
    private Long ventaId;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 120)
    private String clienteNombre;

    @Column(nullable = false, updatable = false)
    private Instant fechaEmision;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    protected Factura() {
        // Requerido por JPA
    }

    public Factura(String numero, Long ventaId, Long clienteId, String clienteNombre,
                   BigDecimal subtotal, BigDecimal iva) {
        this.numero = numero;
        this.ventaId = ventaId;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.fechaEmision = Instant.now();
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = subtotal.add(iva);
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public BigDecimal getTotal() {
        return total;
    }
}

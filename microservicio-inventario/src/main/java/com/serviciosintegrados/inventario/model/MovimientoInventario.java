package com.serviciosintegrados.inventario.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private int cantidad;

    @Column(length = 80)
    private String referencia;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    protected MovimientoInventario() {
        // Requerido por JPA
    }

    public MovimientoInventario(Long productoId, TipoMovimiento tipo, int cantidad, String referencia) {
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.referencia = referencia;
        this.fecha = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getReferencia() {
        return referencia;
    }

    public Instant getFecha() {
        return fecha;
    }
}

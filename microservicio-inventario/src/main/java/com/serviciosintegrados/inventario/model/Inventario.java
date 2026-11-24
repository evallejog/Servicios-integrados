package com.serviciosintegrados.inventario.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Existencias actuales de un producto. El producto vive en otro microservicio, por eso solo se guarda su id.
 */
@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productoId;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private Instant actualizadoEn;

    protected Inventario() {
        // Requerido por JPA
    }

    public Inventario(Long productoId) {
        this.productoId = productoId;
        this.cantidad = 0;
        this.actualizadoEn = Instant.now();
    }

    public boolean hayExistencias(int solicitadas) {
        return solicitadas <= cantidad;
    }

    public void aumentar(int unidades) {
        this.cantidad += unidades;
        this.actualizadoEn = Instant.now();
    }

    public void disminuir(int unidades) {
        if (!hayExistencias(unidades)) {
            throw new IllegalStateException("No hay existencias suficientes");
        }
        this.cantidad -= unidades;
        this.actualizadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}

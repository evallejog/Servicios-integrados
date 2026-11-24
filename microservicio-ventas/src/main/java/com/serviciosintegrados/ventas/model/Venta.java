package com.serviciosintegrados.ventas.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador legible que también se envía a Inventario como referencia del movimiento. */
    @Column(nullable = false, unique = true, length = 40)
    private String referencia;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 120)
    private String clienteNombre;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoVenta estado;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItem> items = new ArrayList<>();

    protected Venta() {
        // Requerido por JPA
    }

    public static Venta registrar(String referencia, Long clienteId, String clienteNombre, List<VentaItem> items) {
        Venta venta = new Venta();
        venta.referencia = referencia;
        venta.clienteId = clienteId;
        venta.clienteNombre = clienteNombre;
        venta.fecha = Instant.now();
        venta.estado = EstadoVenta.REGISTRADA;
        items.forEach(venta::agregarItem);
        venta.total = venta.items.stream()
                .map(VentaItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return venta;
    }

    private void agregarItem(VentaItem item) {
        item.asignarVenta(this);
        this.items.add(item);
    }

    public void marcarFacturada() {
        this.estado = EstadoVenta.FACTURADA;
    }

    public Long getId() {
        return id;
    }

    public String getReferencia() {
        return referencia;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public Instant getFecha() {
        return fecha;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<VentaItem> getItems() {
        return items;
    }
}

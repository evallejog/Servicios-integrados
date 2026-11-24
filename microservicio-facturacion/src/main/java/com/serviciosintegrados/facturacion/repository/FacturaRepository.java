package com.serviciosintegrados.facturacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serviciosintegrados.facturacion.model.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    boolean existsByVentaId(Long ventaId);

    Optional<Factura> findByVentaId(Long ventaId);

    List<Factura> findAllByOrderByFechaEmisionDesc();
}

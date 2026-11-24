package com.serviciosintegrados.ventas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.serviciosintegrados.ventas.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @EntityGraph(attributePaths = "items")
    List<Venta> findAllByOrderByFechaDesc();

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<Venta> findById(Long id);
}

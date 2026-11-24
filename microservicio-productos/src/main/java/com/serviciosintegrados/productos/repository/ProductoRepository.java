package com.serviciosintegrados.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serviciosintegrados.productos.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByNombre();

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}

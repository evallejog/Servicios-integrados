package com.serviciosintegrados.inventario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serviciosintegrados.inventario.model.Inventario;

import jakarta.persistence.LockModeType;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoId(Long productoId);

    /**
     * Bloquea la fila durante la transacción para que dos ventas simultáneas no descuenten el mismo stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventario i where i.productoId = :productoId")
    Optional<Inventario> buscarParaActualizar(@Param("productoId") Long productoId);
}

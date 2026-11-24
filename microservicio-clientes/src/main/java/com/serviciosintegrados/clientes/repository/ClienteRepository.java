package com.serviciosintegrados.clientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serviciosintegrados.clientes.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

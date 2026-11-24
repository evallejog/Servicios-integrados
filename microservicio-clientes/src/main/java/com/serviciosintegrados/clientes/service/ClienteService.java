package com.serviciosintegrados.clientes.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serviciosintegrados.clientes.dto.ClienteRequest;
import com.serviciosintegrados.clientes.dto.ClienteResponse;
import com.serviciosintegrados.clientes.error.ConflictoException;
import com.serviciosintegrados.clientes.error.RecursoNoEncontradoException;
import com.serviciosintegrados.clientes.model.Cliente;
import com.serviciosintegrados.clientes.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repositorio.findAll(Sort.by("nombre")).stream()
                .map(ClienteResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.desde(buscar(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest datos) {
        String documento = datos.documento().trim();
        String email = normalizarEmail(datos.email());

        if (repositorio.existsByDocumento(documento)) {
            throw new ConflictoException("Ya existe un cliente con el documento " + documento);
        }
        if (repositorio.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe un cliente con el correo " + email);
        }

        Cliente cliente = new Cliente();
        aplicar(cliente, datos, documento, email);
        return ClienteResponse.desde(repositorio.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest datos) {
        Cliente cliente = buscar(id);
        String documento = datos.documento().trim();
        String email = normalizarEmail(datos.email());

        if (repositorio.existsByDocumentoAndIdNot(documento, id)) {
            throw new ConflictoException("Ya existe otro cliente con el documento " + documento);
        }
        if (repositorio.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictoException("Ya existe otro cliente con el correo " + email);
        }

        aplicar(cliente, datos, documento, email);
        return ClienteResponse.desde(repositorio.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el cliente con id " + id);
        }
        repositorio.deleteById(id);
    }

    private Cliente buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
    }

    private static void aplicar(Cliente cliente, ClienteRequest datos, String documento, String email) {
        cliente.setNombre(datos.nombre().trim());
        cliente.setDocumento(documento);
        cliente.setEmail(email);
        cliente.setTelefono(datos.telefono());
        cliente.setDireccion(datos.direccion());
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

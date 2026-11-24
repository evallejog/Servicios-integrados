package com.serviciosintegrados.productos.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serviciosintegrados.productos.dto.ProductoRequest;
import com.serviciosintegrados.productos.dto.ProductoResponse;
import com.serviciosintegrados.productos.error.ConflictoException;
import com.serviciosintegrados.productos.error.RecursoNoEncontradoException;
import com.serviciosintegrados.productos.model.Producto;
import com.serviciosintegrados.productos.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean soloActivos) {
        List<Producto> productos = soloActivos
                ? repositorio.findByActivoTrueOrderByNombre()
                : repositorio.findAll(Sort.by("nombre"));
        return productos.stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest datos) {
        String sku = normalizarSku(datos.sku());
        if (repositorio.existsBySku(sku)) {
            throw new ConflictoException("Ya existe un producto con el SKU " + sku);
        }

        Producto producto = new Producto();
        aplicar(producto, datos, sku);
        producto.setActivo(datos.activo() == null || datos.activo());
        return ProductoResponse.desde(repositorio.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest datos) {
        Producto producto = buscar(id);
        String sku = normalizarSku(datos.sku());
        if (repositorio.existsBySkuAndIdNot(sku, id)) {
            throw new ConflictoException("Ya existe otro producto con el SKU " + sku);
        }

        aplicar(producto, datos, sku);
        if (datos.activo() != null) {
            producto.setActivo(datos.activo());
        }
        return ProductoResponse.desde(repositorio.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el producto con id " + id);
        }
        repositorio.deleteById(id);
    }

    private Producto buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    private static void aplicar(Producto producto, ProductoRequest datos, String sku) {
        producto.setSku(sku);
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion());
        producto.setPrecio(datos.precio());
    }

    private static String normalizarSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }
}

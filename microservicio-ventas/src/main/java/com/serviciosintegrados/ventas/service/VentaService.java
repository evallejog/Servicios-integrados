package com.serviciosintegrados.ventas.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serviciosintegrados.ventas.client.ClienteRemoto;
import com.serviciosintegrados.ventas.client.ClientesClient;
import com.serviciosintegrados.ventas.client.InventarioClient;
import com.serviciosintegrados.ventas.client.ProductoRemoto;
import com.serviciosintegrados.ventas.client.ProductosClient;
import com.serviciosintegrados.ventas.dto.ItemRequest;
import com.serviciosintegrados.ventas.dto.VentaRequest;
import com.serviciosintegrados.ventas.dto.VentaResponse;
import com.serviciosintegrados.ventas.error.RecursoNoEncontradoException;
import com.serviciosintegrados.ventas.error.ReglaNegocioException;
import com.serviciosintegrados.ventas.model.Venta;
import com.serviciosintegrados.ventas.model.VentaItem;
import com.serviciosintegrados.ventas.repository.VentaRepository;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final VentaRepository ventas;
    private final ClientesClient clientes;
    private final ProductosClient productos;
    private final InventarioClient inventario;

    public VentaService(VentaRepository ventas, ClientesClient clientes,
                        ProductosClient productos, InventarioClient inventario) {
        this.ventas = ventas;
        this.clientes = clientes;
        this.productos = productos;
        this.inventario = inventario;
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listar() {
        return ventas.findAllByOrderByFechaDesc().stream().map(VentaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        return VentaResponse.desde(buscar(id));
    }

    /**
     * Registra una venta: valida cliente y productos, descuenta el inventario y guarda la venta.
     * No se abre una transacción de base de datos alrededor de las llamadas remotas. Si algo falla después
     * de haber descontado stock, se devuelven las unidades ya descontadas (compensación).
     */
    public VentaResponse registrar(VentaRequest solicitud) {
        ClienteRemoto cliente = clientes.obtener(solicitud.clienteId());
        List<VentaItem> items = construirItems(solicitud.items());
        String referencia = "VTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        List<VentaItem> descontados = new ArrayList<>();
        try {
            for (VentaItem item : items) {
                inventario.registrarSalida(item.getProductoId(), item.getCantidad(), referencia);
                descontados.add(item);
            }
            Venta venta = Venta.registrar(referencia, cliente.id(), cliente.nombre(), items);
            return VentaResponse.desde(ventas.save(venta));
        } catch (RuntimeException ex) {
            compensar(descontados, referencia);
            throw ex;
        }
    }

    /** Marca la venta como facturada. Es idempotente: repetir la llamada no produce errores. */
    @Transactional
    public VentaResponse marcarFacturada(Long id) {
        Venta venta = buscar(id);
        venta.marcarFacturada();
        return VentaResponse.desde(ventas.save(venta));
    }

    private Venta buscar(Long id) {
        return ventas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la venta con id " + id));
    }

    private List<VentaItem> construirItems(List<ItemRequest> solicitados) {
        // Si el mismo producto llega en varias líneas, se consolidan en una sola
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : solicitados) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        List<VentaItem> items = new ArrayList<>();
        cantidades.forEach((productoId, cantidad) -> {
            ProductoRemoto producto = productos.obtener(productoId);
            if (!producto.activo()) {
                throw new ReglaNegocioException("El producto " + producto.nombre() + " no está disponible para la venta");
            }
            items.add(new VentaItem(producto.id(), producto.nombre(), cantidad, producto.precio()));
        });
        return items;
    }

    private void compensar(List<VentaItem> descontados, String referencia) {
        for (VentaItem item : descontados) {
            try {
                inventario.registrarEntrada(item.getProductoId(), item.getCantidad(), "REVERSA-" + referencia);
            } catch (RuntimeException ex) {
                // Se sigue con el resto de líneas; este caso requiere revisión manual
                log.error("No se pudo revertir el stock del producto {} (venta {}): {}",
                        item.getProductoId(), referencia, ex.getMessage());
            }
        }
    }
}

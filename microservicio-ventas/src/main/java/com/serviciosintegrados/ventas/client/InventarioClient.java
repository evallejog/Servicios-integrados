package com.serviciosintegrados.ventas.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.serviciosintegrados.ventas.error.ConflictoException;
import com.serviciosintegrados.ventas.error.ServicioNoDisponibleException;

@Component
public class InventarioClient {

    private final RestClient http;

    public InventarioClient(@Qualifier("inventarioRestClient") RestClient http) {
        this.http = http;
    }

    /** Descuenta unidades del inventario. Lanza {@link ConflictoException} si no hay existencias suficientes. */
    public void registrarSalida(Long productoId, int cantidad, String referencia) {
        enviar("salida", productoId, cantidad, referencia);
    }

    /** Devuelve unidades al inventario (se usa para revertir una venta que no pudo completarse). */
    public void registrarEntrada(Long productoId, int cantidad, String referencia) {
        enviar("entrada", productoId, cantidad, referencia);
    }

    private void enviar(String operacion, Long productoId, int cantidad, String referencia) {
        try {
            http.post()
                    .uri("/api/inventario/{productoId}/" + operacion, productoId)
                    .body(new MovimientoRemoto(cantidad, referencia))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict ex) {
            throw new ConflictoException("Stock insuficiente para el producto " + productoId);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de inventario no está disponible", ex);
        }
    }
}

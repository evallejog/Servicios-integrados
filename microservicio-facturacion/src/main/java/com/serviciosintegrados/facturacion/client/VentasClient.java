package com.serviciosintegrados.facturacion.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.serviciosintegrados.facturacion.error.RecursoNoEncontradoException;
import com.serviciosintegrados.facturacion.error.ServicioNoDisponibleException;

@Component
public class VentasClient {

    private final RestClient http;

    public VentasClient(@Qualifier("ventasRestClient") RestClient http) {
        this.http = http;
    }

    public VentaRemota obtener(Long ventaId) {
        try {
            return http.get()
                    .uri("/api/ventas/{id}", ventaId)
                    .retrieve()
                    .body(VentaRemota.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("La venta " + ventaId + " no existe");
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de ventas no está disponible", ex);
        }
    }

    public void marcarFacturada(Long ventaId) {
        try {
            http.patch()
                    .uri("/api/ventas/{id}/facturada", ventaId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de ventas no está disponible", ex);
        }
    }
}

package com.serviciosintegrados.ventas.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.serviciosintegrados.ventas.error.RecursoNoEncontradoException;
import com.serviciosintegrados.ventas.error.ServicioNoDisponibleException;

@Component
public class ProductosClient {

    private final RestClient http;

    public ProductosClient(@Qualifier("productosRestClient") RestClient http) {
        this.http = http;
    }

    public ProductoRemoto obtener(Long id) {
        try {
            return http.get()
                    .uri("/api/productos/{id}", id)
                    .retrieve()
                    .body(ProductoRemoto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("El producto " + id + " no existe");
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de productos no está disponible", ex);
        }
    }
}

package com.serviciosintegrados.ventas.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.serviciosintegrados.ventas.error.RecursoNoEncontradoException;
import com.serviciosintegrados.ventas.error.ServicioNoDisponibleException;

@Component
public class ClientesClient {

    private final RestClient http;

    public ClientesClient(@Qualifier("clientesRestClient") RestClient http) {
        this.http = http;
    }

    public ClienteRemoto obtener(Long id) {
        try {
            return http.get()
                    .uri("/api/clientes/{id}", id)
                    .retrieve()
                    .body(ClienteRemoto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("El cliente " + id + " no existe");
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de clientes no está disponible", ex);
        }
    }
}

package com.serviciosintegrados.ventas.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Un {@link RestClient} por microservicio dependiente, con tiempos de espera acotados
 * para que una dependencia lenta no deje colgado a Ventas.
 */
@Configuration
public class ServiciosRemotosConfig {

    @Bean
    RestClient clientesRestClient(@Value("${servicios.clientes.url}") String url,
                                  @Value("${servicios.timeout-ms}") long timeoutMs) {
        return crear(url, timeoutMs);
    }

    @Bean
    RestClient productosRestClient(@Value("${servicios.productos.url}") String url,
                                   @Value("${servicios.timeout-ms}") long timeoutMs) {
        return crear(url, timeoutMs);
    }

    @Bean
    RestClient inventarioRestClient(@Value("${servicios.inventario.url}") String url,
                                    @Value("${servicios.timeout-ms}") long timeoutMs) {
        return crear(url, timeoutMs);
    }

    private static RestClient crear(String urlBase, long timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .build();
    }
}

package com.serviciosintegrados.clientes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.serviciosintegrados.clientes.dto.ClienteRequest;
import com.serviciosintegrados.clientes.dto.ClienteResponse;
import com.serviciosintegrados.clientes.error.ConflictoException;
import com.serviciosintegrados.clientes.error.RecursoNoEncontradoException;
import com.serviciosintegrados.clientes.model.Cliente;
import com.serviciosintegrados.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repositorio;

    @InjectMocks
    private ClienteService servicio;

    private static ClienteRequest solicitud() {
        return new ClienteRequest("  Ana Torres ", " 1020304050 ", "  Ana.Torres@Correo.com ",
                "3001234567", "Calle 10 # 20-30, Bogotá");
    }

    @Test
    void crear_normalizaDatosYGuardaElCliente() {
        when(repositorio.existsByDocumento("1020304050")).thenReturn(false);
        when(repositorio.existsByEmailIgnoreCase("ana.torres@correo.com")).thenReturn(false);
        when(repositorio.save(any(Cliente.class))).thenAnswer(invocacion -> {
            Cliente guardado = invocacion.getArgument(0);
            ReflectionTestUtils.setField(guardado, "id", 7L);
            return guardado;
        });

        ClienteResponse respuesta = servicio.crear(solicitud());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(repositorio).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Ana Torres");
        assertThat(captor.getValue().getEmail()).isEqualTo("ana.torres@correo.com");
        assertThat(respuesta.id()).isEqualTo(7L);
        assertThat(respuesta.documento()).isEqualTo("1020304050");
    }

    @Test
    void crear_conDocumentoDuplicado_lanzaConflicto() {
        when(repositorio.existsByDocumento("1020304050")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(solicitud()))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("1020304050");
        verify(repositorio, never()).save(any());
    }

    @Test
    void crear_conCorreoDuplicado_lanzaConflicto() {
        when(repositorio.existsByDocumento("1020304050")).thenReturn(false);
        when(repositorio.existsByEmailIgnoreCase("ana.torres@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(solicitud()))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("correo");
        verify(repositorio, never()).save(any());
    }

    @Test
    void obtener_conIdInexistente_lanzaNoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizar_conDocumentoDeOtroCliente_lanzaConflicto() {
        Cliente existente = new Cliente();
        when(repositorio.findById(5L)).thenReturn(Optional.of(existente));
        when(repositorio.existsByDocumentoAndIdNot("1020304050", 5L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.actualizar(5L, solicitud()))
                .isInstanceOf(ConflictoException.class);
        verify(repositorio, never()).save(any());
    }

    @Test
    void eliminar_conIdInexistente_lanzaNoEncontrado() {
        when(repositorio.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> servicio.eliminar(42L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repositorio, never()).deleteById(any());
    }
}

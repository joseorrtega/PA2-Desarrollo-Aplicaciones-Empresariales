
package com.isil.edu.pe.spedidos;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.impl.AnulacionPedidoServiceImpl;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnulacionPedidoServiceImplTest {

    private PedidoRepository pedidoRepository;
    private AnulacionPedidoRepository anulacionRepository;
    private DevolucionStockClient devolucionStockClient;
    private AnulacionPedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        anulacionRepository = mock(AnulacionPedidoRepository.class);
        devolucionStockClient = mock(DevolucionStockClient.class);

        service = new AnulacionPedidoServiceImpl(
                pedidoRepository,
                anulacionRepository,
                devolucionStockClient
        );
    }

    @Test
    void debeAnularPedidoYGuardarDevolucion() {
        Pedido pedido = crearPedido();
        DevolucionStockResponse respuesta = new DevolucionStockResponse(
                "MON-001", 1, 20
        );

        when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedido));
        when(anulacionRepository.findByPedidoId(2L)).thenReturn(List.of());
        when(devolucionStockClient.devolverStock(
                eq("MON-001"), any(DevolucionStockRequest.class)))
                .thenReturn(respuesta);
        when(anulacionRepository.save(any(AnulacionPedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnulacionPedido resultado = service.anularPedido(2L, "Prueba");

        assertNotNull(resultado);
        assertEquals(PedidoEstado.ANULADO, pedido.getEstado());
        verify(anulacionRepository).save(any(AnulacionPedido.class));
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void debeRechazarAnulacionDuplicadaSinLlamarInventario() {
        Pedido pedido = crearPedido();
        AnulacionPedido anterior = mock(AnulacionPedido.class);

        when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedido));
        when(anulacionRepository.findByPedidoId(2L))
                .thenReturn(List.of(anterior));

        assertThrows(
                PedidoException.class,
                () -> service.anularPedido(2L, "Segunda solicitud")
        );

        verifyNoInteractions(devolucionStockClient);
        verify(anulacionRepository, never()).save(any(AnulacionPedido.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void noDebeGuardarAnulacionSiInventarioFalla() {
        Pedido pedido = crearPedido();

        when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedido));
        when(anulacionRepository.findByPedidoId(2L)).thenReturn(List.of());
        when(devolucionStockClient.devolverStock(
                eq("MON-001"), any(DevolucionStockRequest.class)))
                .thenThrow(new InventarioClientException(
                        500, "Inventario no disponible"));

        assertThrows(
                InventarioNoDisponibleException.class,
                () -> service.anularPedido(2L, "Prueba de error")
        );

        verify(anulacionRepository, never()).save(any(AnulacionPedido.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado());
    }

    private Pedido crearPedido() {
        Pedido pedido = new Pedido();
        pedido.setId(2L);
        pedido.setCliente("Prueba Error");
        pedido.setProductoCodigo("MON-001");
        pedido.setProductoNombre("Monitor");
        pedido.setPrecioUnitario(new BigDecimal("850.00"));
        pedido.setCantidad(1);
        pedido.setEstado(PedidoEstado.REGISTRADO);
        return pedido;
    }
}

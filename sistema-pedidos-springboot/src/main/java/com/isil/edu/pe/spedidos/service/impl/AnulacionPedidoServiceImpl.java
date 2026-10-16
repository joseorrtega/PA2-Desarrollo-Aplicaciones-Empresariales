
package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

    private final PedidoRepository pedidoRepository;
    private final AnulacionPedidoRepository anulacionPedidoRepository;
    private final DevolucionStockClient devolucionStockClient;

    public AnulacionPedidoServiceImpl(
            PedidoRepository pedidoRepository,
            AnulacionPedidoRepository anulacionPedidoRepository,
            DevolucionStockClient devolucionStockClient) {
        this.pedidoRepository = pedidoRepository;
        this.anulacionPedidoRepository = anulacionPedidoRepository;
        this.devolucionStockClient = devolucionStockClient;
    }

    @Override
    @Transactional
    public AnulacionPedido anularPedido(Long pedidoId, String motivo) {

        if (pedidoId == null) {
            throw new PedidoException("El ID del pedido es obligatorio.");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new PedidoException("El pedido no existe."));

        if (pedido.getEstado() != PedidoEstado.REGISTRADO) {
            throw new PedidoException(
                    "Solo se pueden anular pedidos registrados.");
        }

        if (!anulacionPedidoRepository.findByPedidoId(pedidoId).isEmpty()) {
            throw new PedidoException(
                    "El pedido ya tiene una anulación registrada.");
        }

        DevolucionStockRequest request =
                new DevolucionStockRequest(pedido.getCantidad());

        DevolucionStockResponse respuesta;

        try {
            respuesta = devolucionStockClient.devolverStock(
                    pedido.getProductoCodigo(),
                    request);
        } catch (InventarioClientException e) {
            if (e.getStatus() >= 500) {
                throw new InventarioNoDisponibleException(
                        "El servicio de inventario no pudo completar la devolución.",
                        e);
            }

            throw new PedidoException(
                    "No se pudo devolver el stock: " + e.getMessage(), e);
        }

        if (respuesta == null) {
            throw new InventarioNoDisponibleException(
                    "El servicio de inventario no devolvió una respuesta válida.");
        }

        AnulacionPedido anulacion = new AnulacionPedido(
                pedido.getId(),
                respuesta.getCodigo(),
                respuesta.getCantidadDevuelta(),
                LocalDateTime.now(),
                motivo);

        AnulacionPedido guardada =
                anulacionPedidoRepository.save(anulacion);

        pedido.setEstado(PedidoEstado.ANULADO);
        pedidoRepository.save(pedido);

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnulacionPedido> listarAnulaciones() {
        return anulacionPedidoRepository.findAll();
    }
}

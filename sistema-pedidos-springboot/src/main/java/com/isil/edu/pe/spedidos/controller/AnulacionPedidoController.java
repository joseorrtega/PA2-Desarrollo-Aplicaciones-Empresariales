
package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

    private final AnulacionPedidoService anulacionPedidoService;

    public AnulacionPedidoController(
            AnulacionPedidoService anulacionPedidoService) {
        this.anulacionPedidoService = anulacionPedidoService;
    }


    @PostMapping
    public ResponseEntity<AnulacionPedidoResponse> anularPedido(
            @RequestBody AnulacionPedidoRequest request) {
        try {
            AnulacionPedido anulacion =
                    anulacionPedidoService.anularPedido(
                            request.getPedidoId(),
                            request.getMotivo());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(AnulacionPedidoResponse.from(anulacion));

        } catch (InventarioNoDisponibleException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de inventario no está disponible o excedió el tiempo de espera.",
                    e);
        }
    }

    @GetMapping
    public ResponseEntity<List<AnulacionPedidoResponse>> listarAnulaciones() {

        List<AnulacionPedidoResponse> respuestas =
                anulacionPedidoService.listarAnulaciones()
                        .stream()
                        .map(AnulacionPedidoResponse::from)
                        .toList();

        return ResponseEntity.ok(respuestas);
    }
}

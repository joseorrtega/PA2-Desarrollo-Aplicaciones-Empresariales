
package com.isil.edu.pe.spedidos.controller.dto;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.time.LocalDateTime;

public class AnulacionPedidoResponse {

    private Long id;
    private Long pedidoId;
    private String productoCodigo;
    private int cantidadDevuelta;
    private LocalDateTime fecha;
    private String motivo;

    public AnulacionPedidoResponse() {
    }

    public static AnulacionPedidoResponse from(AnulacionPedido anulacion) {
        AnulacionPedidoResponse response = new AnulacionPedidoResponse();
        response.setId(anulacion.getId());
        response.setPedidoId(anulacion.getPedidoId());
        response.setProductoCodigo(anulacion.getProductoCodigo());
        response.setCantidadDevuelta(anulacion.getCantidadDevuelta());
        response.setFecha(anulacion.getFecha());
        response.setMotivo(anulacion.getMotivo());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getProductoCodigo() {
        return productoCodigo;
    }

    public void setProductoCodigo(String productoCodigo) {
        this.productoCodigo = productoCodigo;
    }

    public int getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public void setCantidadDevuelta(int cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

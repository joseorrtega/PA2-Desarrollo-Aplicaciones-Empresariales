
package com.isil.edu.pe.spedidos.controller.dto;

public class AnulacionPedidoRequest {

    private Long pedidoId;
    private String motivo;

    public AnulacionPedidoRequest() {
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}


package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DevolucionStockRestClient implements DevolucionStockClient {

    private final RestClient restClient;
    private final String baseUrl;

    public DevolucionStockRestClient(
            @Qualifier("inventarioHttpClient") RestClient restClient,
            @Value("${inventario.api.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public DevolucionStockResponse devolverStock(
            String codigo,
            DevolucionStockRequest request) {
        try {
            return restClient
                    .post()
                    .uri(baseUrl + "/productos/codigo/{codigo}/devoluciones", codigo)
                    .body(request)
                    .retrieve()
                    .body(DevolucionStockResponse.class);

        } catch (RestClientResponseException e) {
            throw new InventarioClientException(
                    e.getStatusCode().value(),
                    "Error al devolver stock: HTTP "
                            + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            throw new InventarioNoDisponibleException(
                    "El servicio de inventario no está disponible o excedió el tiempo de espera.",
                    e);
        }
    }
}

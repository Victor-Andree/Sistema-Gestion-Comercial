package com.gestion.sgc.domain.ports.inputs;

import com.gestion.sgc.application.dto.request.StockRequest;
import com.gestion.sgc.application.dto.response.StockResponse;

import java.util.List;
import java.util.Optional;

public interface StockIn {

    StockResponse createInitialStock(StockRequest request);

    Optional<StockResponse> getStockByProductId(Long productId);

    List<StockResponse> listAllStocks();

    List<StockResponse> listLowStock();

    StockResponse updateStock(Long productId, Integer quantity, String type);

    StockResponse adjustStock(Long productId, Integer realQuantity, String observation);

    boolean checkAvailability(Long productId, Integer requiredQuantity);


}

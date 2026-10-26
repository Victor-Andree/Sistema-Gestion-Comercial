package com.gestion.sgc;

import com.gestion.sgc.domain.aggregates.model.Stock;
import com.gestion.sgc.infraestructure.adapters.StockRepositoryAdapter;
import com.gestion.sgc.infraestructure.entity.StockEntity;
import com.gestion.sgc.infraestructure.mapper.StockMapper;
import com.gestion.sgc.infraestructure.repository.StockJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockRepositoryAdapterTests {
    @Mock StockJpaRepository repository;
    @Mock StockMapper mapper;
    @InjectMocks StockRepositoryAdapter adapter;

    @Test
    void findsStockByProductIdInsteadOfStockId() {
        StockEntity entity = new StockEntity();
        Stock expected = new Stock();
        when(repository.findByProductoProductoId(14L)).thenReturn(Optional.of(entity));
        when(mapper.toDomainFromEntity(entity)).thenReturn(expected);

        assertSame(expected, adapter.findByProductoId(14L).orElseThrow());

        verify(repository).findByProductoProductoId(14L);
        verify(repository, never()).findById(14L);
    }
}

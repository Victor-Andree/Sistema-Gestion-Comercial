package com.gestion.sgc.infraestructure.adapters;

import com.gestion.sgc.domain.aggregates.model.Stock;
import com.gestion.sgc.domain.ports.outputs.StockRepositoryPort;
import com.gestion.sgc.infraestructure.entity.StockEntity;
import com.gestion.sgc.infraestructure.mapper.StockMapper;
import com.gestion.sgc.infraestructure.repository.StockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockRepositoryPort {

    private final StockJpaRepository jpaRepository;
    private final StockMapper stockMapper;

    @Override
    public Stock save(Stock stock) {
        StockEntity entity = stockMapper.toEntity(stock);
        StockEntity saved = jpaRepository.save(entity);
        return stockMapper.toDomainFromEntity(saved);
    }

    @Override
    public Optional<Stock> findById(Long id) {
        return jpaRepository.findById(id)
                .map(stockMapper::toDomainFromEntity);
    }

    @Override
    public Optional<Stock> findByProductoId(Long productId) {
        return jpaRepository.findById(productId)
                .map(stockMapper::toDomainFromEntity);
    }

    @Override
    public List<Stock> findAll() {
        return jpaRepository.findAll().stream()
                .map(stockMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Stock> findByStockBajo() {
        return jpaRepository.findStockBajo().stream()
                .map(stockMapper::toDomainFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByProductoId(Long productId) {
        return jpaRepository.existsById(productId);
    }




}

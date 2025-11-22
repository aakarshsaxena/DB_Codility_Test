package com.example.demo.repository;

import com.example.demo.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findTopByTradeIdOrderByVersionDesc(String tradeId);
    Optional<Trade> findByTradeIdAndVersion(String tradeId, Integer version);
}

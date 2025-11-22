package com.example.demo.service;

import com.example.demo.model.Trade;
import com.example.demo.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    public Trade createTrade(Trade trade) throws Exception {
        // Validate maturity date
        if (trade.getMaturityDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Maturity date cannot be in the past");
        }

        Optional<Trade> existingTradeOpt = tradeRepository.findTopByTradeIdOrderByVersionDesc(trade.getTradeId());
        if (existingTradeOpt.isPresent()) {
            Trade existingTrade = existingTradeOpt.get();
            if (trade.getVersion() <= existingTrade.getVersion()) {
                throw new IllegalArgumentException("Version is lower than the current version");
            }
        }

        trade.setCreatedDate(LocalDate.now());
        return tradeRepository.save(trade);
    }

    public Optional<Trade> getLatestTrade(String tradeId) {
        return tradeRepository.findTopByTradeIdOrderByVersionDesc(tradeId);
    }

    public Optional<Trade> getTradeByVersion(String tradeId, Integer version) {
        return tradeRepository.findByTradeIdAndVersion(tradeId, version);
    }

}

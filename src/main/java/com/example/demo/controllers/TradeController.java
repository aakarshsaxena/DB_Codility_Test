package com.example.demo.controllers;

import com.example.demo.model.Trade;
import com.example.demo.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

    private final TradeService tradeService;

    TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<?> createTrade(@Valid @RequestBody Trade trade) {
        try {
            Trade createdTrade = tradeService.createTrade(trade);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTrade);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Maturity date cannot be in the past")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            } else if (e.getMessage().equals("Version is lower than the current version")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<?> getLatestTrade(@PathVariable String tradeId) {
        Optional<Trade> trade = tradeService.getLatestTrade(tradeId);

        if (trade.isPresent()) {
            return ResponseEntity.ok(trade.get());  // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Trade not found for tradeId: " + tradeId);  // 404 Not Found
        }
    }

    @GetMapping("/{tradeId}/versions/{version}")
    public ResponseEntity<?> getTradeByVersion(@PathVariable String tradeId, @PathVariable Integer version) {
        Optional<Trade> trade = tradeService.getTradeByVersion(tradeId, version);

        if (trade.isPresent()) {
            return ResponseEntity.ok(trade.get());  // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Trade not found for tradeId: " + tradeId + " with version: " + version);  // 404 Not Found
        }
    }



}

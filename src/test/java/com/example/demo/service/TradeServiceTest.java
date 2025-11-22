package com.example.demo.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;

import com.example.demo.model.Trade;
import com.example.demo.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeService tradeService;

    private Trade trade;
    private Trade existingTrade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trade = new Trade();
        trade.setTradeId("T1");
        trade.setMaturityDate(LocalDate.now().plusDays(1));
//        trade.setVersion(1);
        existingTrade =  new Trade();
        existingTrade.setTradeId("T1");
        existingTrade.setVersion(2);
        existingTrade.setMaturityDate(LocalDate.now().plusDays(1));
    }

    // Test: Create Trade when maturity date is in the past
    @Test
    void testCreateTradeWithMaturityDateInPast() {
        // Setting maturity date in the past
        trade.setMaturityDate(LocalDate.now().minusDays(1));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.createTrade(trade);
        });
        assertEquals("Maturity date cannot be in the past", thrown.getMessage());
    }

    // Test: Create Trade with valid maturity date
    @Test
    void testCreateTradeWithValidMaturityDate() throws Exception {
        // Mock the trade repository to return empty for the latest trade
        when(tradeRepository.findTopByTradeIdOrderByVersionDesc("T1")).thenReturn(Optional.empty());

        Trade createdTrade = tradeService.createTrade(trade);
        assertNotNull(createdTrade);
        assertEquals(trade.getTradeId(), createdTrade.getTradeId());
        verify(tradeRepository, times(1)).save(trade);  // Verify that save was called once
    }

    // Test: Create Trade when version is lower than the current version
    @Test
    void testCreateTradeWithLowerVersion() {
        // Mock the repository to return an existing trade with a higher version
        when(tradeRepository.findTopByTradeIdOrderByVersionDesc("T1")).thenReturn(Optional.of(existingTrade));

        trade.setVersion(1); // Setting the trade version to 1, which is lower than the existing version (2)

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.createTrade(trade);
        });
        assertEquals("Version is lower than the current version", thrown.getMessage());
    }

    // Test: Create Trade when version is greater than the current version
    @Test
    void testCreateTradeWithHigherVersion() throws Exception {
        // Mock the repository to return an existing trade with a version 1
        when(tradeRepository.findTopByTradeIdOrderByVersionDesc("T1")).thenReturn(Optional.of(existingTrade));

        trade.setVersion(3); // Setting version greater than existing version (2)

        // The trade should be created successfully
        Trade createdTrade = tradeService.createTrade(trade);
        assertNotNull(createdTrade);
        assertEquals(3, createdTrade.getVersion());
        verify(tradeRepository, times(1)).save(trade);  // Verify that save was called once
    }

    // Test: Get Latest Trade when no trades exist
    @Test
    void testGetLatestTradeWhenNoneExist() {
        when(tradeRepository.findTopByTradeIdOrderByVersionDesc("T1")).thenReturn(Optional.empty());

        Optional<Trade> result = tradeService.getLatestTrade("T1");
        assertTrue(result.isEmpty());
    }

    // Test: Get Latest Trade when a trade exists
    @Test
    void testGetLatestTradeWhenExists() {
        when(tradeRepository.findTopByTradeIdOrderByVersionDesc("T1")).thenReturn(Optional.of(existingTrade));

        Optional<Trade> result = tradeService.getLatestTrade("T1");
        assertTrue(result.isPresent());
        assertEquals(existingTrade, result.get());
    }

    // Test: Get Trade by Version when trade exists
    @Test
    void testGetTradeByVersion() {
        when(tradeRepository.findByTradeIdAndVersion("T1", 2)).thenReturn(Optional.of(existingTrade));

        Optional<Trade> result = tradeService.getTradeByVersion("T1", 2);
        assertTrue(result.isPresent());
        assertEquals(existingTrade, result.get());
    }

    // Test: Get Trade by Version when trade does not exist
    @Test
    void testGetTradeByVersionNotFound() {
        when(tradeRepository.findByTradeIdAndVersion("T1", 3)).thenReturn(Optional.empty());

        Optional<Trade> result = tradeService.getTradeByVersion("T1", 3);
        assertTrue(result.isEmpty());
    }
}


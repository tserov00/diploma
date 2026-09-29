package org.example.marketservice.service;

import org.example.marketservice.dto.response.MarketCandleResponseDto;
import org.example.marketservice.dto.response.MarketQuoteResponseDto;
import org.example.marketservice.dto.response.MarketSecuritiesResponseDto;
import org.example.marketservice.entity.Security;
import org.example.marketservice.mapper.MarketMapper;
import org.example.marketservice.marketdata.MarketCandle;
import org.example.marketservice.marketdata.MarketDataProvider;
import org.example.marketservice.marketdata.MarketQuote;
import org.example.marketservice.repository.SecurityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class MarketService {
    private static final Logger log = LoggerFactory.getLogger(MarketService.class);
    private final SecurityRepository securityRepository;
    private final MarketMapper marketMapper;
    private final MarketDataProvider marketDataProvider;

    public MarketService(SecurityRepository securityRepository, MarketMapper marketMapper, MarketDataProvider marketDataProvider) {
        this.securityRepository = securityRepository;
        this.marketMapper = marketMapper;
        this.marketDataProvider = marketDataProvider;
    }

    @Transactional(readOnly = true)
    public List<MarketSecuritiesResponseDto> getSecurities() {
        return securityRepository.findAll().stream().map(marketMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<MarketSecuritiesResponseDto> getSecuritiesBatchByIds(List<Long> ids) {
        return securityRepository.findAllById(ids).stream().map(marketMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public MarketSecuritiesResponseDto getSecurityById(Long id) {
        return marketMapper.toResponseDto(findSecurity(id));
    }

    @Transactional
    public MarketQuoteResponseDto getQuoteById(Long id) {
        Security security = findSecurity(id);
        refreshQuote(security);
        return toQuoteDto(security);
    }

    @Transactional(readOnly = true)
    public List<MarketCandleResponseDto> getHistory(Long id, String timeframe, long from, long to) {
        Security security = findSecurity(id);
        List<MarketCandle> candles = marketDataProvider.getHistory(
                security.getExchangeCode(), security.getTicker(), timeframe,
                java.time.Instant.ofEpochSecond(from), java.time.Instant.ofEpochSecond(to));
        return candles.stream().map(c -> new MarketCandleResponseDto(c.time(), c.open(), c.high(), c.low(), c.close(), c.volume())).toList();
    }

    @Transactional
    @Scheduled(fixedDelayString = "${market.data.refresh-interval-ms:60000}", initialDelayString = "${market.data.initial-refresh-delay-ms:1000}")
    public void refreshQuotes() {
        for (Security security : securityRepository.findAll()) {
            refreshQuote(security);
        }
    }

    private void refreshQuote(Security security) {
        try {
            MarketQuote quote = marketDataProvider.getQuote(security.getExchangeCode(), security.getTicker());
            security.setLastPrice(quote.lastPrice());
            security.setBidPrice(quote.bidPrice());
            security.setAskPrice(quote.askPrice());
            security.setBidVolume(quote.bidVolumeShares());
            security.setAskVolume(quote.askVolumeShares());
            security.setLotSize(quote.lotSize());
            security.setDayHigh(quote.dayHigh());
            security.setDayLow(quote.dayLow());
            security.setDayVolume(quote.dayVolume());
            security.setLastPriceAt(quote.lastPriceAt());
            security.setQuoteUpdatedAt(quote.quoteUpdatedAt());
            security.setQuoteReceivedAt(quote.quoteReceivedAt());
            security.setQuoteSource(quote.source());
            security.setSnapshotKey(quote.snapshotKey());
        } catch (Exception e) {
            // A failed provider request leaves the last known snapshot visible with its original timestamp.
            log.warn("Не удалось обновить котировку {} через {}: {}", security.getTicker(), marketDataProvider.sourceName(), e.getMessage());
        }
    }

    private Security findSecurity(Long id) {
        return securityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Инструмент не найден"));
    }

    private MarketQuoteResponseDto toQuoteDto(Security security) {
        MarketQuoteResponseDto dto = new MarketQuoteResponseDto();
        dto.setSecurityId(security.getId());
        dto.setTicker(security.getTicker());
        dto.setLastPrice(security.getLastPrice());
        dto.setBidPrice(security.getBidPrice());
        dto.setAskPrice(security.getAskPrice());
        dto.setBidVolume(security.getBidVolume());
        dto.setAskVolume(security.getAskVolume());
        dto.setLotSize(security.getLotSize());
        dto.setDayHigh(security.getDayHigh());
        dto.setDayLow(security.getDayLow());
        dto.setDayVolume(security.getDayVolume());
        dto.setLastPriceAt(security.getLastPriceAt());
        dto.setQuoteUpdatedAt(security.getQuoteUpdatedAt());
        dto.setQuoteReceivedAt(security.getQuoteReceivedAt());
        dto.setSource(security.getQuoteSource());
        dto.setDelayed("ALOR".equals(security.getQuoteSource()));
        dto.setSnapshotKey(security.getSnapshotKey());
        return dto;
    }
}

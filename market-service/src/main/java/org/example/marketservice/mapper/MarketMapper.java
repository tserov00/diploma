package org.example.marketservice.mapper;

import org.example.marketservice.dto.response.MarketSecuritiesResponseDto;
import org.example.marketservice.entity.Security;
import org.springframework.stereotype.Component;

@Component
public class MarketMapper {
    public MarketSecuritiesResponseDto toResponseDto(Security security) {
        if (security == null) return null;
        MarketSecuritiesResponseDto dto = new MarketSecuritiesResponseDto();
        dto.setId(security.getId());
        dto.setTicker(security.getTicker());
        dto.setCompanyName(security.getCompanyName());
        dto.setIsin(security.getIsin());
        dto.setCurrencyCode(security.getCurrencyCode());
        dto.setStockExchangeName(security.getExchangeCode());
        dto.setLotSize(security.getLotSize());
        dto.setLastPrice(security.getLastPrice());
        dto.setBidPrice(security.getBidPrice());
        dto.setAskPrice(security.getAskPrice());
        dto.setBidVolume(security.getBidVolume());
        dto.setAskVolume(security.getAskVolume());
        dto.setDayHigh(security.getDayHigh());
        dto.setDayLow(security.getDayLow());
        dto.setDayVolume(security.getDayVolume());
        dto.setLastPriceAt(security.getLastPriceAt());
        dto.setQuoteUpdatedAt(security.getQuoteUpdatedAt());
        dto.setQuoteReceivedAt(security.getQuoteReceivedAt());
        dto.setQuoteSource(security.getQuoteSource());
        return dto;
    }
}

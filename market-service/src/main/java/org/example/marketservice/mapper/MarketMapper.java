package org.example.marketservice.mapper;

import org.example.marketservice.dto.response.MarketSecuritiesResponseDto;
import org.example.marketservice.dto.response.MarketQuoteResponseDto;
import org.example.marketservice.entity.Security;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MarketMapper {
    @Mapping(source = "exchangeCode", target = "stockExchangeName")
    MarketSecuritiesResponseDto toResponseDto(Security security);

    @Mapping(source = "id", target = "securityId")
    @Mapping(source = "quoteSource", target = "source")
    @Mapping(target = "delayed", expression = "java(\"ALOR\".equals(security.getQuoteSource()))")
    MarketQuoteResponseDto toQuoteResponseDto(Security security);
}

package org.example.orderservice.mapper;

import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.PortfolioResponseDto;
import org.example.orderservice.entity.CustomerPortfolio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerPortfolioMapper {
    @Mapping(source = "security.id", target = "securityId")
    @Mapping(source = "security.ticker", target = "ticker")
    @Mapping(source = "security.companyName", target = "companyName")
    @Mapping(source = "security.lastPrice", target = "currentPrice")
    @Mapping(source = "security.currencyCode", target = "currencyCode")
    PortfolioResponseDto toResponseDto(CustomerPortfolio customerPortfolio, SecurityDto security);
}

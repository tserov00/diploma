package org.example.orderservice.mapper;

import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.TransactionResponseDto;
import org.example.orderservice.entity.Transaction;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "ticker", expression = "java(resolveTicker(transaction.getOrder().getSecurityId(), securities))")
    @Mapping(target = "companyName", expression = "java(resolveCompanyName(transaction.getOrder().getSecurityId(), securities))")
    @Mapping(target = "side", expression = "java(transaction.getSide().name())")
    @Mapping(target = "orderType", expression = "java(transaction.getOrderType().name())")
    @Mapping(target = "orderId", source = "order.id")
    TransactionResponseDto toResponseDto(Transaction transaction, @Context Map<Long, SecurityDto> securities);

    List<TransactionResponseDto> toResponseDtos(List<Transaction> transactions,
                                                @Context Map<Long, SecurityDto> securities);

    default String resolveTicker(Long securityId, Map<Long, SecurityDto> securities) {
        SecurityDto security = securities.get(securityId);
        return security == null ? "-" : security.getTicker();
    }

    default String resolveCompanyName(Long securityId, Map<Long, SecurityDto> securities) {
        SecurityDto security = securities.get(securityId);
        return security == null ? null : security.getCompanyName();
    }
}

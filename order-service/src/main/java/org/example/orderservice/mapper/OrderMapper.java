package org.example.orderservice.mapper;

import org.example.orderservice.dto.request.SecurityDto;
import org.example.orderservice.dto.response.OrderResponseDto;
import org.example.orderservice.entity.Order;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "ticker", expression = "java(resolveTicker(order.getSecurityId(), securities))")
    @Mapping(target = "companyName", expression = "java(resolveCompanyName(order.getSecurityId(), securities))")
    @Mapping(target = "side", expression = "java(order.getSide().name())")
    @Mapping(target = "orderType", expression = "java(order.getOrderType().name())")
    @Mapping(target = "marketPrice", expression = "java(resolveMarketPrice(order.getSecurityId(), securities))")
    @Mapping(target = "filledQuantity", expression = "java(order.getQuantity() - order.getRemainingQuantity())")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderResponseDto toResponseDto(Order order, @Context Map<Long, SecurityDto> securities);

    List<OrderResponseDto> toResponseDtos(List<Order> orders, @Context Map<Long, SecurityDto> securities);

    default String resolveTicker(Long securityId, Map<Long, SecurityDto> securities) {
        SecurityDto security = securities.get(securityId);
        return security == null ? "-" : security.getTicker();
    }

    default String resolveCompanyName(Long securityId, Map<Long, SecurityDto> securities) {
        SecurityDto security = securities.get(securityId);
        return security == null ? null : security.getCompanyName();
    }

    default BigDecimal resolveMarketPrice(Long securityId, Map<Long, SecurityDto> securities) {
        SecurityDto security = securities.get(securityId);
        return security == null ? null : security.getLastPrice();
    }
}

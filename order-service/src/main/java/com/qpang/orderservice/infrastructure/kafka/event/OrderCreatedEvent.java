package com.qpang.orderservice.infrastructure.kafka.event;

import java.util.UUID;
import java.util.List;

public record OrderCreatedEvent(
        UUID orderId,
        UUID supplyCompanyId,
        UUID requestCompanyId,
        UUID userId,
        List<OrderItemEvent> items
) {
    public record OrderItemEvent(UUID productId, int quantity) {}
}
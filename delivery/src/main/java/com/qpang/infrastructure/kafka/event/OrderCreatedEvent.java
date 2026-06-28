package com.qpang.infrastructure.kafka.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID supplyCompanyId,
        UUID requestCompanyId,
        UUID userId,
        List<OrderItemEvent> items
) {
    public record OrderItemEvent(UUID productId, int quantity) {}
}
package com.qpang.infrastructure.kafka.event;

import java.util.UUID;

public record DeliveryFailedEvent(
        UUID orderId,
        String reason
) {}
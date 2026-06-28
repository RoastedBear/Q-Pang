package com.qpang.orderservice.infrastructure.kafka;

import com.qpang.orderservice.application.OrderService;
import com.qpang.orderservice.infrastructure.kafka.event.DeliveryFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "delivery.failed", groupId = "order-service")
    public void handleDeliveryFailed(DeliveryFailedEvent event) {
        log.warn("[Kafka] delivery.failed 이벤트 수신 - orderId: {}, reason: {}",
                event.orderId(), event.reason());
        orderService.cancelOrderBySystem(event.orderId(), event.reason());
    }
}
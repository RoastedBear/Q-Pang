package com.qpang.infrastructure.kafka;

import com.qpang.application.service.DeliveryService;
import com.qpang.infrastructure.client.dto.CreateDeliveryCommand;
import com.qpang.infrastructure.kafka.event.DeliveryFailedEvent;
import com.qpang.infrastructure.kafka.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final DeliveryService deliveryService;
    private final DeliveryEventProducer deliveryEventProducer;

    @KafkaListener(topics = "order.created", groupId = "delivery-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[Kafka] order.created 이벤트 수신 - orderId: {}", event.orderId());
        try {
            CreateDeliveryCommand command = new CreateDeliveryCommand(
                    event.orderId(),
                    event.supplyCompanyId(),
                    event.requestCompanyId()
            );
            deliveryService.createDelivery(command, event.userId(), "MASTER");
            log.info("[Kafka] 배송 생성 성공 - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("[Kafka] 배송 생성 실패 - orderId: {}, reason: {}", event.orderId(), e.getMessage());
            deliveryEventProducer.sendDeliveryFailed(
                    new DeliveryFailedEvent(event.orderId(), e.getMessage())
            );
        }
    }
}
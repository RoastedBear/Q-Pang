package com.qpang.orderservice.infrastructure.kafka;

import com.qpang.orderservice.infrastructure.kafka.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order.created";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event.orderId().toString(), event);
        log.info("[Kafka] order.created 이벤트 발행 - orderId: {}", event.orderId());
    }
}
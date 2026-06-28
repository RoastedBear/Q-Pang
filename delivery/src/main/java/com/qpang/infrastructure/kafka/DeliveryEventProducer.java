package com.qpang.infrastructure.kafka;

import com.qpang.infrastructure.kafka.event.DeliveryFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private static final String DELIVERY_FAILED_TOPIC = "delivery.failed";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeliveryFailed(DeliveryFailedEvent event) {
        kafkaTemplate.send(DELIVERY_FAILED_TOPIC, event.orderId().toString(), event);
        log.warn("[Kafka] delivery.failed 이벤트 발행 - orderId: {}, reason: {}",
                event.orderId(), event.reason());
    }
}
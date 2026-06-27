package com.qpang.infrastructure.client;

import com.qpang.infrastructure.client.dto.HubRouteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class HubServiceClientFallback implements HubServiceClient {

    @Override
    public List<HubRouteResponse> getHubRoute(UUID sourceHubId, UUID destinationHubId) {
        log.warn("[CircuitBreaker] hub-service 호출 실패 - sourceHubId: {}, destinationHubId: {}",
                sourceHubId, destinationHubId);
        throw new RuntimeException("허브 경로 조회 서비스가 일시적으로 불가합니다. 잠시 후 다시 시도해주세요.");
    }
}
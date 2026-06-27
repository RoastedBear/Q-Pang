package com.qpang.infrastructure.client;

import com.qpang.infrastructure.client.dto.HubRouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "hub-service",
        url = "http://localhost:8085",
        fallback = HubServiceClientFallback.class
)
public interface HubServiceClient {

    @GetMapping("/api/hub-routes/path")
    List<HubRouteResponse> getHubRoute(
            @RequestParam("sourceHubId") UUID sourceHubId,
            @RequestParam("destinationHubId") UUID destinationHubId
    );
}
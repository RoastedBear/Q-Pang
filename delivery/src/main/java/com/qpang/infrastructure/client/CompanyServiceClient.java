package com.qpang.infrastructure.client;

import com.qpang.common.response.APIResponse;
import com.qpang.infrastructure.client.dto.CompanyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "company-service",
        url = "http://localhost:8083",
        fallback = CompanyServiceClientFallback.class
)
public interface CompanyServiceClient {

    @GetMapping("/companies/{id}")
    APIResponse<CompanyResponse> getCompany(@PathVariable("id") UUID id,
                                            @RequestHeader("X-User-Id") UUID userId,
                                            @RequestHeader("X-User-Role") String role);
}
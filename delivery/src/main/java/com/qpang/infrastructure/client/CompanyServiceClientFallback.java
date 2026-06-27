package com.qpang.infrastructure.client;

import com.qpang.common.response.APIResponse;
import com.qpang.infrastructure.client.dto.CompanyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class CompanyServiceClientFallback implements CompanyServiceClient {

    @Override
    public APIResponse<CompanyResponse> getCompany(UUID id, UUID userId, String role) {
        log.warn("[CircuitBreaker] company-service 호출 실패 - companyId: {}", id);
        throw new RuntimeException("company-service 일시적으로 사용 불가합니다. 잠시 후 다시 시도해주세요.");
    }
}
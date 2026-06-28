package com.qpang.infrastructure.client.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor  // 추가
public class CreateDeliveryCommand {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID supplyCompanyId;

    @NotNull
    private UUID requestCompanyId;
}
package com.example.maum.account.dto;

import lombok.Builder;

@Builder
public record ExistsDTO(
        boolean exists,
        int authNumber
) {}

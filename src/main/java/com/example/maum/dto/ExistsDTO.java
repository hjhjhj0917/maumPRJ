package com.example.maum.dto;

import lombok.Builder;

@Builder
public record ExistsDTO(
        boolean exists,
        int authNumber
) {}

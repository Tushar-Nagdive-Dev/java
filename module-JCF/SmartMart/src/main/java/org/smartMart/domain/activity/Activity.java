package org.smartMart.domain.activity;

import java.time.Instant;

public record Activity(
        String userId,
        String action,
        Instant at
) {}

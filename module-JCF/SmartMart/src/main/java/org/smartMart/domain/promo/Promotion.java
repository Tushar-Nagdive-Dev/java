package org.smartMart.domain.promo;

import java.time.Instant;

/**
 * Model: Promotion (priority, startAt)
 *Collections (later services): PriorityQueue<Promotion>.
 */
public record Promotion(
        String id,
        Integer priority,
        Instant startAt
) {}

package org.smartMart.ports;

import org.smartMart.domain.promo.Promotion;

import java.time.Instant;
import java.util.Optional;

public interface PromotionPort {

    void add(Promotion promotion);

    Optional<Promotion> peekActive(Instant now);
}

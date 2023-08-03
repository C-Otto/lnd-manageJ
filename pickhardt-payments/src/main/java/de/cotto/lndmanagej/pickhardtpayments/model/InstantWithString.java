package de.cotto.lndmanagej.pickhardtpayments.model;

import java.time.Instant;

public record InstantWithString(Instant instant, String string) {
    InstantWithString(String string) {
        this(Instant.now(), string);
    }
}

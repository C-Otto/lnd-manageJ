package de.cotto.lndmanagej.model;

import java.time.Instant;

public record MissionControlEntry(Pubkey source, Pubkey target, Coins amount, Instant time, boolean failure) {
    public boolean success() {
        return !failure;
    }

    public boolean isAfter(Instant threshold) {
        return time.isAfter(threshold);
    }
}

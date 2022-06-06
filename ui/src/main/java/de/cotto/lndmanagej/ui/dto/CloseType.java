package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.BreachForceClosedChannel;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.CoopClosedChannel;
import de.cotto.lndmanagej.model.ForceClosedChannel;

import javax.annotation.Nullable;

public enum CloseType {

    COOP_CLOSE("coop"),
    FORCE_CLOSE("force"),
    BREACH_FORCE_CLOSE("breach"),
    UNKNOWN("unknown");

    private final String displayName;

    CloseType(String displayName) {
        this.displayName = displayName;
    }

    public static CloseType getType(@Nullable ClosedChannel channel) {
        if (channel instanceof BreachForceClosedChannel) {
            return BREACH_FORCE_CLOSE;
        } else if (channel instanceof ForceClosedChannel) {
            return FORCE_CLOSE;
        } else if (channel instanceof CoopClosedChannel) {
            return COOP_CLOSE;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

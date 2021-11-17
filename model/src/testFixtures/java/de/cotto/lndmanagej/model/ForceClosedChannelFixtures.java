package de.cotto.lndmanagej.model;

public class ForceClosedChannelFixtures {
    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_REMOTE =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder()).build();

    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_LOCAL =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
            .withCloseInitiator(CloseInitiator.LOCAL)
            .build();

    public static final BreachForceClosedChannel FORCE_CLOSED_CHANNEL_BREACH =
            ClosedChannelFixtures.getWithDefaults(new BreachForceClosedChannelBuilder()).build();
}

package de.cotto.lndmanagej.model;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ResolutionFixtures.RESOLUTION;
import static de.cotto.lndmanagej.model.ResolutionFixtures.RESOLUTION_2;

public class ForceClosedChannelFixtures {
    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
                    .withResolutions(Set.of(RESOLUTION, RESOLUTION_2))
                    .build();

    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_2 =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
                    .withChannelId(CHANNEL_ID_2)
                    .build();

    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_REMOTE =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder()).build();

    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_LOCAL =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
            .withCloseInitiator(CloseInitiator.LOCAL)
            .build();

    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_OPEN_LOCAL =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
                    .withOpenInitiator(OpenInitiator.LOCAL)
                    .build();

    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_OPEN_REMOTE =
            ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
                    .withOpenInitiator(OpenInitiator.REMOTE)
                    .build();

    public static final BreachForceClosedChannel FORCE_CLOSED_CHANNEL_BREACH =
            ClosedChannelFixtures.getWithDefaults(new BreachForceClosedChannelBuilder())
                    .withResolutions(Set.of(RESOLUTION_2))
                    .build();
}

package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;

public class CoopClosedChannelFixtures {
    public static final CoopClosedChannel CLOSED_CHANNEL =
            ClosedChannelFixtures.getWithDefaults(new CoopClosedChannelBuilder())
                    .build();

    public static final CoopClosedChannel CLOSED_CHANNEL_2 =
            ClosedChannelFixtures.getWithDefaults(new CoopClosedChannelBuilder())
                    .withChannelId(CHANNEL_ID_2)
                    .withOpenInitiator(OpenInitiator.UNKNOWN)
                    .withCloseInitiator(CloseInitiator.LOCAL)
                    .build();

    public static final CoopClosedChannel CLOSED_CHANNEL_3 =
            ClosedChannelFixtures.getWithDefaults(new CoopClosedChannelBuilder())
                    .withChannelId(CHANNEL_ID_3)
                    .withCloseInitiator(CloseInitiator.UNKNOWN)
                    .build();

    public static final CoopClosedChannel CLOSED_CHANNEL_TO_NODE_3 =
            ClosedChannelFixtures.getWithDefaults(new CoopClosedChannelBuilder())
                    .withChannelId(CHANNEL_ID_3)
                    .withRemotePubkey(PUBKEY_3)
                    .build();

}

package de.cotto.lndmanagej.model;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public class PaymentAttemptHopFixtures {
    public static final PaymentAttemptHop PAYMENT_ATTEMPT_HOP =
            new PaymentAttemptHop(Optional.of(CHANNEL_ID), Coins.ofMilliSatoshis(1), Optional.of(PUBKEY));
}

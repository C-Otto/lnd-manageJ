package de.cotto.lndmanagej.model;

import java.util.Optional;

public record PaymentAttemptHop(Optional<ChannelId> channelId, Coins amount, Optional<Pubkey> targetPubkey) {
}

package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;

public record Edge(ChannelId channelId, Pubkey startNode, Pubkey endNode, Coins capacity) {
}

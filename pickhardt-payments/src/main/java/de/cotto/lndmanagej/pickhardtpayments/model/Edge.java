package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;

public record Edge(ChannelId channelId, Pubkey startNode, Pubkey endNode, Coins capacity, Policy policy) {
    public Edge withCapacity(Coins capacity) {
        return new Edge(channelId(), startNode(), endNode(), capacity, policy());
    }
}

package de.cotto.lndmanagej.model;

import java.util.Objects;

public class LocalOpenChannel extends LocalChannel {
    private final BalanceInformation balanceInformation;
    private final Coins totalSent;
    private final Coins totalReceived;
    private final boolean active;
    private final long numUpdates;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public LocalOpenChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            BalanceInformation balanceInformation,
            OpenInitiator openInitiator,
            Coins totalSent,
            Coins totalReceived,
            boolean isPrivate,
            boolean active,
            long numUpdates
    ) {
        super(channelCoreInformation, ownPubkey, remotePubkey, openInitiator, isPrivate);
        this.balanceInformation = balanceInformation;
        this.totalSent = totalSent;
        this.totalReceived = totalReceived;
        this.active = active;
        this.numUpdates = numUpdates;
    }

    public BalanceInformation getBalanceInformation() {
        return balanceInformation;
    }

    public long getNumUpdates() {
        return numUpdates;
    }

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(isPrivateChannel(), active, false, OpenCloseStatus.OPEN);
    }

    @Override
    public Coins getTotalSent() {
        return totalSent;
    }

    @Override
    public Coins getTotalReceived() {
        return totalReceived;
    }

    @Override
    @SuppressWarnings("CPD-START")
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        LocalOpenChannel that = (LocalOpenChannel) other;
        return active == that.active
                && Objects.equals(balanceInformation, that.balanceInformation)
                && Objects.equals(totalSent, that.totalSent)
                && Objects.equals(totalReceived, that.totalReceived)
                && numUpdates == that.numUpdates;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balanceInformation, totalSent, totalReceived, active, numUpdates);
    }
}
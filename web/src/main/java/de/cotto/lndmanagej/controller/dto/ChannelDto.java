package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDto(
        String channelIdShort,
        String channelIdCompact,
        String channelIdCompactLnd,
        ChannelPoint channelPoint,
        int openHeight,
        Pubkey remotePubkey,
        String capacitySat,
        String totalSentSat,
        String totalReceivedSat,
        ChannelStatusDto status,
        OpenInitiator openInitiator,
        ClosedChannelDetailsDto closeDetails,
        long numUpdates,
        String minHtlcConstraintMsat
) {
    public ChannelDto(LocalChannel localChannel) {
        this(
                String.valueOf(localChannel.getId().getShortChannelId()),
                localChannel.getId().getCompactForm(),
                localChannel.getId().getCompactFormLnd(),
                localChannel.getChannelPoint(),
                localChannel.getId().getBlockHeight(),
                localChannel.getRemotePubkey(),
                satString(localChannel.getCapacity()),
                satString(localChannel.getTotalSent()),
                satString(localChannel.getTotalReceived()),
                ChannelStatusDto.createFromModel(localChannel.getStatus()),
                localChannel.getOpenInitiator(),
                ClosedChannelDetailsDto.createFromModel(localChannel),
                localChannel instanceof LocalOpenChannel local ? local.getNumUpdates() : 0L,
                localChannel instanceof LocalOpenChannel local ?  msatString(local.getMinHtlcConstraint()) : "0"
        );
    }

    private static String satString(Coins coins) {
        return String.valueOf(coins.satoshis());
    }

    private static String msatString(Coins coins) {
        return String.valueOf(coins.milliSatoshis());
    }
}

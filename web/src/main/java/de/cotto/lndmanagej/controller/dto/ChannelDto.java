package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDto(
        String channelIdShort,
        String channelIdCompact,
        String channelIdCompactLnd,
        ChannelPoint channelPoint,
        int openHeight,
        Pubkey remotePubkey,
        String capacity,
        String totalSent,
        String totalReceived,
        ChannelStatusDto status,
        OpenInitiator openInitiator,
        ClosedChannelDetailsDto closeDetails
) {
    public ChannelDto(LocalChannel localChannel, ClosedChannelDetailsDto closeDetails) {
        this(
                String.valueOf(localChannel.getId().getShortChannelId()),
                localChannel.getId().getCompactForm(),
                localChannel.getId().getCompactFormLnd(),
                localChannel.getChannelPoint(),
                localChannel.getId().getBlockHeight(),
                localChannel.getRemotePubkey(),
                String.valueOf(localChannel.getCapacity().satoshis()),
                String.valueOf(localChannel.getTotalSent().satoshis()),
                String.valueOf(localChannel.getTotalReceived().satoshis()),
                ChannelStatusDto.createFrom(localChannel.getStatus()),
                localChannel.getOpenInitiator(),
                closeDetails
        );
    }
}

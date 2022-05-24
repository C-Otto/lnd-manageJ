package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;

public record OpenChannelDto(
        ChannelId channelId,
        String remoteAlias,
        Pubkey remotePubkey,
        PoliciesDto policies,
        BalanceInformationModel balanceInformation,
        long capacitySat
) {

    public double getOutboundPercentage() {
        return balanceInformation().getOutboundPercentage();
    }

}

package de.cotto.lndmanagej.ui.dto.warning;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.List;

public record DashboardWarningDto(
        String alias,
        Pubkey pubkey,
        List<String> nodeWarnings,
        List<ChannelWarningDto> channelWarnings
) {

}
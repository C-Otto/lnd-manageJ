package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;

public record OpenChannelDto(
        ChannelId channelId,
        String remoteAlias,
        Pubkey remotePubkey,
        PoliciesDto policies,
        BalanceInformationDto balanceInformation,
        long capacitySat
) {

    public String getRatio() {
        double percentLocal = getOutboundPercentage();
        double percentRemote = 100 - percentLocal;
        int leftDots = (int) (percentRemote / 5);
        int rightDots = (int) (percentLocal / 5);
        return dots(leftDots) + " | " + dots(rightDots);
    }

    private String dots(int numberOfDots) {
        if (numberOfDots == 0) {
            return "";
        }
        String dotsString = "· ".repeat(numberOfDots);
        return dotsString.substring(0, dotsString.length() - 1); // remove excess space
    }

    public double getOutboundPercentage() {
        long outbound = getOutbound();
        long routableCapacity = outbound + getInbound();
        return (1.0 * outbound / routableCapacity) * 100;
    }

    public long getOutbound() {
        return Long.parseLong(balanceInformation.localBalanceSat());
    }

    public long getInbound() {
        return Long.parseLong(balanceInformation.remoteBalanceSat());
    }
}

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

    public String getRatio() {
        double percentLocal = getOutboundPercentage();
        double percentRemote = 100 - percentLocal;
        int leftDots = (int) (percentRemote / 5);
        int rightDots = (int) (percentLocal / 5);
        return dots(leftDots) + " | " + dots(rightDots);
    }

    public double getOutboundPercentage() {
        return balanceInformation().getOutboundPercentage();
    }

    private String dots(int numberOfDots) {
        if (numberOfDots == 0) {
            return "";
        }
        String dotsString = "· ".repeat(numberOfDots);
        return dotsString.substring(0, dotsString.length() - 1); // remove excess space
    }

}

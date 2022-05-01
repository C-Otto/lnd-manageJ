package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;

import java.text.NumberFormat;

public record OpenChannelDto(
        ChannelId channelId,
        String remoteAlias,
        Pubkey remotePubkey,
        PoliciesDto policies,
        BalanceInformationDto balanceInformation) {

    public String getRatio() {
        int local = (int) getOutboundPercentage();
        int remote = 100 - local;
        int leftDots = (int) (remote / 5d);
        int rightDots = (int) (local / 5d);
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
        return (outbound / (double) routableCapacity) * 100;
    }

    public long getOutbound() {
        return Long.parseLong(balanceInformation.localBalanceSat());
    }

    public long getInbound() {
        return Long.parseLong(balanceInformation.remoteBalanceSat());
    }

    public String formatOutbound() {
        return NumberFormat.getInstance().format(getOutbound());
    }

    public String formatInbound() {
        return NumberFormat.getInstance().format(getInbound());
    }
}

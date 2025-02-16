package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.BalanceInformation;

import java.text.DecimalFormat;

public record BalanceInformationModel(
        long localBalanceSat,
        long localReserveSat,
        long localAvailableSat,
        long remoteBalanceSat,
        long remoteReserveSat,
        long remoteAvailableSat
) {

    private static final int TEN_PERCENT = 10;
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("###"));
    public static final BalanceInformationModel EMPTY = createFromModel(BalanceInformation.EMPTY);

    public static BalanceInformationModel createFromModel(BalanceInformation balanceInformation) {
        return new BalanceInformationModel(
                balanceInformation.localBalance().milliSatoshis() / 1_000,
                balanceInformation.localReserve().milliSatoshis() / 1_000,
                balanceInformation.localAvailable().milliSatoshis() / 1_000,
                balanceInformation.remoteBalance().milliSatoshis() / 1_000,
                balanceInformation.remoteReserve().milliSatoshis() / 1_000,
                balanceInformation.remoteAvailable().milliSatoshis() / 1_000
        );
    }

    public double getOutboundPercentage() {
        long outbound = localBalanceSat();
        long routableCapacity = outbound + remoteBalanceSat();
        return 1.0 * outbound / routableCapacity * 100;
    }

    public long getRoutableCapacity() {
        return remoteBalanceSat() + localBalanceSat();
    }

    public double getInboundPercentage() {
        return 100 - getOutboundPercentage();
    }

    public String getOutboundPercentageLabel() {
        double outbound = getOutboundPercentage();
        return outbound < TEN_PERCENT ? "" : DECIMAL_FORMAT.get().format(outbound) + "%";
    }

    public String getInboundPercentageLabel() {
        double inbound = getInboundPercentage();
        return inbound < TEN_PERCENT ? "" : DECIMAL_FORMAT.get().format(inbound) + "%";
    }
}

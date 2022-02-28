package de.cotto.lndmanagej.model.warnings;

public record ChannelBalanceFluctuationWarning(
        int minLocalBalancePercentage,
        int maxLocalBalancePercentage,
        int days
) implements ChannelWarning {
    @Override
    public String description() {
        return "Channel balance ranged from %d%% to %d%% in the past %d days".formatted(
                minLocalBalancePercentage,
                maxLocalBalancePercentage,
                days
        );
    }
}

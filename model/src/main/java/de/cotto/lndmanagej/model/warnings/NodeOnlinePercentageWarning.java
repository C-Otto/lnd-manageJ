package de.cotto.lndmanagej.model.warnings;

public record NodeOnlinePercentageWarning(int onlinePercentage, int days) implements NodeWarning {
    @Override
    public String description() {
        return "Node has been online %d%% in the past %d days".formatted(onlinePercentage, days);
    }
}

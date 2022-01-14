package de.cotto.lndmanagej.model.warnings;

public record NodeOnlinePercentageWarning(int onlinePercentage) implements NodeWarning {
    @Override
    public String description() {
        return "Node has been online " + onlinePercentage + "% in the last week";
    }
}

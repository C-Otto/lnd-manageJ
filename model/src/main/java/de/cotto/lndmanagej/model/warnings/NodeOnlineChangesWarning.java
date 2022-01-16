package de.cotto.lndmanagej.model.warnings;

public record NodeOnlineChangesWarning(int changes, int days) implements NodeWarning {
    @Override
    public String description() {
        return "Node changed between online and offline %d times in the past %d days".formatted(changes, days);
    }
}

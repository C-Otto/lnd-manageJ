package de.cotto.lndmanagej.model.warnings;

public record NodeOnlineChangesWarning(int changes) implements NodeWarning {
    @Override
    public String description() {
        return "Node changed between online and offline " + changes + " times";
    }
}

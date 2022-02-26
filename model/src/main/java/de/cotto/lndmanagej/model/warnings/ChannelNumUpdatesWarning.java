package de.cotto.lndmanagej.model.warnings;

public record ChannelNumUpdatesWarning(long numUpdates) implements ChannelWarning {
    @Override
    public String description() {
        return "Channel has accumulated %,d updates".formatted(numUpdates);
    }
}

package de.cotto.lndmanagej.model.warnings;

public record NodeNoFlowWarning(int days) implements NodeWarning {
    @Override
    public String description() {
        return "No flow in the past " + days + " days";
    }
}

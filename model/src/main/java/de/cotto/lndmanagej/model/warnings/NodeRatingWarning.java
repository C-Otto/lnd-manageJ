package de.cotto.lndmanagej.model.warnings;

public record NodeRatingWarning(long rating, long threshold) implements NodeWarning {
    @Override
    public String description() {
        return "Rating of %,d is below threshold of %,d".formatted(rating, threshold);
    }
}

package de.cotto.lndmanagej.configuration;

public enum RatingConfigurationSettings implements ConfigurationSetting {
    DAYS_FOR_ANALYSIS("days_for_analysis"),
    MIN_AGE_DAYS_FOR_ANALYSIS("minimum_age_in_days");

    private final String name;

    RatingConfigurationSettings(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSection() {
        return "ratings";
    }
}

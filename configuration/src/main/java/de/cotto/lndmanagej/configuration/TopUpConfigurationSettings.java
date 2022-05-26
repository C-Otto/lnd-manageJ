package de.cotto.lndmanagej.configuration;

public enum TopUpConfigurationSettings implements ConfigurationSetting {
    THRESHOLD("threshold_sat"),
    EXPIRY("expiry_seconds"),
    SLEEP_AFTER_FAILURE_MILLISECONDS("sleep_after_failure_milliseconds"),
    MAX_RETRIES_AFTER_FAILURE("max_retries_after_failure");

    private final String name;

    TopUpConfigurationSettings(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSection() {
        return "top-up";
    }
}

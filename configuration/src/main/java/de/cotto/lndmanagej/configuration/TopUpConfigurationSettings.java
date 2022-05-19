package de.cotto.lndmanagej.configuration;

public enum TopUpConfigurationSettings implements ConfigurationSetting {
    THRESHOLD("threshold_sat");

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

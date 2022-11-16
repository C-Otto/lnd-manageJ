package de.cotto.lndmanagej.configuration;

public enum PickhardtPaymentsConfigurationSettings implements ConfigurationSetting {
    LIQUIDITY_INFORMATION_MAX_AGE("liquidity_information_max_age_in_seconds"),
    USE_MISSION_CONTROL("use_mission_control"),
    QUANTIZATION("quantization"),
    PIECEWISE_LINEAR_APPROXIMATIONS("piecewise_linear_approximations"),
    ENABLED("enabled"),
    MAX_CLTV_EXPIRY("max-cltv-expiry");

    private final String name;

    PickhardtPaymentsConfigurationSettings(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSection() {
        return "pickhardt-payments";
    }
}

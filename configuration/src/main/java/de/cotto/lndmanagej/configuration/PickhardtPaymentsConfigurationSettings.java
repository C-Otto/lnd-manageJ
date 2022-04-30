package de.cotto.lndmanagej.configuration;

public enum PickhardtPaymentsConfigurationSettings implements ConfigurationSetting {
    USE_MISSION_CONTROL("use_mission_control"),
    QUANTIZATION("quantization"),
    PIECEWISE_LINEAR_APPROXIMATIONS("piecewise_linear_approximations");

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

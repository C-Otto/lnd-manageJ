package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;

public record FeeReportDto(String earned) {
    public FeeReportDto(Coins earned) {
        this(String.valueOf(earned.milliSatoshis()));
    }
}

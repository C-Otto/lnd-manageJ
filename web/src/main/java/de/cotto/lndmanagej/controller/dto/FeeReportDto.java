package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;

public record FeeReportDto(String earned, String sourced) {
    public FeeReportDto(Coins earned, Coins sourced) {
        this(
                String.valueOf(earned.milliSatoshis()),
                String.valueOf(sourced.milliSatoshis())
        );
    }
}

package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;

public record FeeReportDto(String earned, String sourced) {
    public FeeReportDto(Coins earned, Coins sourced) {
        this(
                String.valueOf(earned.milliSatoshis()),
                String.valueOf(sourced.milliSatoshis())
        );
    }

    public static FeeReportDto createFrom(FeeReport feeReport) {
        return new FeeReportDto(feeReport.earned(), feeReport.sourced());
    }
}

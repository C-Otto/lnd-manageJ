package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;

public record FeeReportDto(String earnedMilliSat, String sourcedMilliSat) {
    public FeeReportDto(Coins earned, Coins sourced) {
        this(
                String.valueOf(earned.milliSatoshis()),
                String.valueOf(sourced.milliSatoshis())
        );
    }

    public static FeeReportDto createFromModel(FeeReport feeReport) {
        return new FeeReportDto(feeReport.earned(), feeReport.sourced());
    }
}

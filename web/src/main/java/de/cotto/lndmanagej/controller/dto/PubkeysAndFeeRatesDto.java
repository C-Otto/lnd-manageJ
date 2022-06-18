package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.PubkeyAndFeeRate;

import java.util.List;

public record PubkeysAndFeeRatesDto(List<PubkeyAndFeeRateDto> entries) {
    public static PubkeysAndFeeRatesDto create(List<PubkeyAndFeeRate> entries) {
        List<PubkeyAndFeeRateDto> list = entries.stream().map(PubkeyAndFeeRateDto::fromModel).toList();
        return new PubkeysAndFeeRatesDto(list);
    }
}

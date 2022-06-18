package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.PubkeyAndFeeRate;

public record PubkeyAndFeeRateDto(Pubkey pubkey, int feeRate) {
    public static PubkeyAndFeeRateDto fromModel(PubkeyAndFeeRate pubkeyAndFeeRate) {
        return new PubkeyAndFeeRateDto(
                pubkeyAndFeeRate.pubkey(),
                pubkeyAndFeeRate.feeRate()
        );
    }
}
